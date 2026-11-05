import { DurableObject } from "cloudflare:workers";
import privacyHtml from "./privacy.html";
import termsHtml from "./terms.html";

const PORT = 8080;
const HEALTH_URL = "http://container/actuator/health";
// 요청이 끊기고 이 시간이 지나면 컨테이너를 재운다
const INACTIVITY_TIMEOUT_MS = 15 * 60 * 1000;
const STARTUP_TIMEOUT_MS = 180 * 1000;

// 컨테이너에 넘기는 값. 비밀값은 wrangler secret으로 넣는다.
const ENV_NAMES = [
  "SPRING_PROFILES_ACTIVE",
  "DB_URL",
  "DB_USERNAME",
  "DB_PASSWORD",
  "JWT_SECRET",
  "GOOGLE_CLIENT_ID",
  "APPLE_BUNDLE_ID",
  "APPLE_SERVICE_ID",
  "APPLE_REDIRECT_URI",
  "APPLE_TEAM_ID",
  "APPLE_KEY_ID",
  "APPLE_PRIVATE_KEY",
  "STORAGE_BUCKET_NAME",
  "STORAGE_PUBLIC_BASE_URL",
  "STORAGE_ENDPOINT",
  "STORAGE_ACCESS_KEY",
  "STORAGE_SECRET_KEY",
  "FIREBASE_CREDENTIALS_BASE64",
];

export class BackendContainer extends DurableObject {
  starting;
  ready = false;

  constructor(ctx, env) {
    super(ctx, env);
    if (ctx.container.running) {
      void ctx.blockConcurrencyWhile(() =>
        ctx.container.setInactivityTimeout(INACTIVITY_TIMEOUT_MS),
      );
    }
  }

  async fetch(request) {
    // 준비된 뒤에는 요청마다 상태를 확인하지 않는다 (health가 DB까지 확인해 느리다)
    if (!this.ready || !this.ctx.container.running) {
      // 동시에 들어온 요청은 시작과 준비 확인을 함께 기다린다
      this.starting ??= this.startAndWait().finally(() => {
        this.starting = undefined;
      });
      try {
        await this.starting;
      } catch (error) {
        console.error("container start failed", error);
        return Response.json({ message: "server starting" }, { status: 503 });
      }
    }

    const url = new URL(request.url);
    url.protocol = "http:";
    url.host = "container";
    const forwarded = new Request(url, request);
    forwarded.headers.delete("host");
    try {
      return await this.ctx.container.getTcpPort(PORT).fetch(forwarded);
    } catch (error) {
      // 컨테이너가 재시작 중이면 다음 요청에서 다시 준비를 확인한다
      this.ready = false;
      throw error;
    }
  }

  async containerEnv() {
    const env = {};
    for (const name of ENV_NAMES) {
      if (this.env[name]) env[name] = this.env[name];
    }
    return env;
  }

  // 이미지나 환경값이 바뀌면 다른 값이 나온다. 비밀값 원문은 저장하지 않는다.
  async revisionOf(env) {
    const text = JSON.stringify([this.ctx.container.images.base, env]);
    const digest = await crypto.subtle.digest("SHA-256", new TextEncoder().encode(text));
    return btoa(String.fromCharCode(...new Uint8Array(digest)));
  }

  async startAndWait() {
    const container = this.ctx.container;
    const env = await this.containerEnv();
    const revision = await this.revisionOf(env);

    // 실행 중인 컨테이너는 배포해도 옛 이미지로 남으므로 직접 교체한다
    if (container.running && (await this.ctx.storage.get("revision")) !== revision) {
      await container.destroy();
    }
    if (!container.running) {
      container.start({
        image: container.images.base,
        instance: "standard-2",
        enableInternet: true,
        env,
      });
      await this.ctx.storage.put("revision", revision);
    }
    await container.setInactivityTimeout(INACTIVITY_TIMEOUT_MS);

    const port = container.getTcpPort(PORT);
    const deadline = Date.now() + STARTUP_TIMEOUT_MS;
    let lastError;
    while (Date.now() < deadline) {
      try {
        const response = await port.fetch(HEALTH_URL, {
          signal: AbortSignal.timeout(5000),
        });
        await response.body?.cancel();
        if (response.ok) {
          this.ready = true;
          return;
        }
        lastError = new Error(`health ${response.status}`);
      } catch (error) {
        lastError = error;
      }
      await scheduler.wait(500);
    }
    throw new Error("container did not become ready", { cause: lastError });
  }
}

// 공개 문서는 컨테이너를 깨우지 않고 바로 응답한다
const PAGES = { "/privacy": privacyHtml, "/terms": termsHtml };

function backend(env) {
  const id = env.BACKEND.idFromName("main");
  return env.BACKEND.get(id, { locationHint: "apac" });
}

export default {
  fetch(request, env) {
    const page = PAGES[new URL(request.url).pathname];
    if (page) {
      return new Response(page, {
        headers: { "content-type": "text/html; charset=utf-8" },
      });
    }
    return backend(env).fetch(request);
  },

  scheduled(_event, env, ctx) {
    ctx.waitUntil(
      backend(env).fetch("https://internal/actuator/health").then((response) => {
        console.log("daily health", response.status);
      }),
    );
  },
};
