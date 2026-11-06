#!/usr/bin/env python3
"""약관 정본(src/main/resources/terms/*.txt)에서 DB 마이그레이션과 공개 HTML 페이지를 만든다.

사용: python3 deploy/terms/build_terms.py <마이그레이션 번호> <버전>
  - src/main/resources/db/migration/V<번호>__update_terms_content.sql  (terms.content, terms.version 갱신)
  - deploy/oracle/pages/terms.html, privacy.html
약관 본문을 고칠 때는 txt를 고치고 번호와 버전을 올려 다시 실행한다.
"""
import html
import pathlib
import re
import sys

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "src/main/resources/terms"
ORDER = ["TERMS_OF_SERVICE", "PRIVACY_POLICY", "HEALTH_DATA", "LOCATION", "AGE_VERIFICATION", "MARKETING", "SNS"]
CSS = ('body{margin:0;background:#101418;color:#e8edf2;font:16px/1.7 -apple-system,BlinkMacSystemFont,"Apple SD Gothic Neo","Noto Sans KR",sans-serif}'
       'main{max-width:720px;margin:auto;padding:32px 20px 64px}h1{font-size:24px}h2{font-size:18px;margin-top:32px}h3{font-size:16px;margin-top:24px}'
       'ul{padding-left:20px}li{margin:4px 0}a{color:#00eeff}nav a{display:block}hr{border:0;border-top:1px solid #3a444e;margin:48px 0 0}')
URL = re.compile(r"(https?://[^\s)]+)")


def read(code):
    return (SRC / f"{code}.txt").read_text(encoding="utf-8").strip()


def inline(text):
    return URL.sub(lambda m: f'<a href="{m.group(1)}">{m.group(1)}</a>', html.escape(text, quote=False))


def render(text, level, anchor=None):
    """본문 텍스트를 HTML로 바꾼다. 첫 줄은 제목, 나머지 제목 줄은 level+1."""
    lines = text.split("\n")
    title, body = lines[0], lines[1:]
    has_articles = any(re.match(r"제\d+조 ", ln) for ln in body)
    out = [f'<h{level}{f" id={chr(34)}{anchor}{chr(34)}" if anchor else ""}>{html.escape(title)}</h{level}>']
    inner = level + 1
    bullets = []

    def flush():
        if bullets:
            out.append("<ul>" + "".join(f"<li>{inline(b)}</li>" for b in bullets) + "</ul>")
            bullets.clear()

    for i, ln in enumerate(body):
        if not ln.strip():
            flush()
            continue
        if ln.startswith("- "):
            bullets.append(ln[2:])
            continue
        flush()
        nxt = next((x for x in body[i + 1:] if x.strip()), "")
        if re.match(r"제\d+조 ", ln) or ln == "부칙" or (not has_articles and re.match(r"\d+\. ", ln) and nxt.startswith("- ")):
            out.append(f"<h{inner}>{html.escape(ln)}</h{inner}>")
        else:
            out.append(f"<p>{inline(ln)}</p>")
    flush()
    return "\n".join(out)


def page(title, body):
    return (f'<!doctype html><html lang="ko"><head><meta charset="utf-8"><meta name="viewport" content="width=device-width,initial-scale=1">'
            f"<style>{CSS}</style><title>{title}</title></head><body><main>\n{body}\n</main></body></html>\n")


def main():
    num, version = sys.argv[1], int(sys.argv[2])
    sql = ["-- deploy/terms/build_terms.py 가 만든 파일이다. 본문은 src/main/resources/terms/*.txt 를 고쳐 다시 만든다.",
           "ALTER TABLE terms ADD COLUMN IF NOT EXISTS content TEXT;"]
    for code in ORDER:
        text = read(code)
        assert "$terms$" not in text
        sql.append(f"UPDATE terms SET content = $terms${text}$terms$, version = {version}, updated_at = CURRENT_TIMESTAMP WHERE code = '{code}';")
    (ROOT / f"src/main/resources/db/migration/V{num}__update_terms_content.sql").write_text("\n".join(sql) + "\n", encoding="utf-8")

    pages = ROOT / "deploy/oracle/pages"
    privacy = render(read("PRIVACY_POLICY"), 1)
    privacy += '\n<p><a href="/terms">서비스 이용약관</a></p>'
    (pages / "privacy.html").write_text(page("개인정보 처리방침", privacy), encoding="utf-8")

    extra = ["HEALTH_DATA", "LOCATION", "AGE_VERIFICATION", "MARKETING", "SNS"]
    toc = '<nav><a href="#terms">서비스 이용약관</a>' + "".join(
        f'<a href="#{c.lower()}">{html.escape(read(c).split(chr(10))[0])}</a>' for c in extra) + '<a href="/privacy">개인정보 처리방침</a></nav>'
    parts = [render(read("TERMS_OF_SERVICE"), 1, "terms"), toc]
    for c in extra:
        parts.append("<hr>" + render(read(c), 2, c.lower()))
    (pages / "terms.html").write_text(page("서비스 이용약관", "\n".join(parts)), encoding="utf-8")


main()
