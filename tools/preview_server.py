"""Serve the Roborazzi report for local review: thumbnails scaled by CSS and
full images opened in a new tab. Expects a .preview/ directory mirroring the
app module layout (build/reports/roborazzi and src/test/screenshots)."""

import http.server
import os
import sys

ROOT = os.path.normpath(
    os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".preview")
)

INJECT = """
<style>
  img.thumb {
    max-height: 220px;
    width: auto;
    height: auto !important;
    object-fit: contain !important;
    cursor: zoom-in;
  }
</style>
<script>
  document.querySelectorAll('img.modal-trigger').forEach(function (img) {
    var src = img.getAttribute('src');
    var clean = img.cloneNode(false);
    clean.className = 'thumb';
    clean.setAttribute('src', src);
    clean.removeAttribute('data-alt');
    var a = document.createElement('a');
    a.href = src;
    a.target = '_blank';
    a.rel = 'noopener';
    a.appendChild(clean);
    img.parentNode.replaceChild(a, img);
  });
</script>
"""


class Handler(http.server.SimpleHTTPRequestHandler):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, directory=ROOT, **kwargs)

    def do_GET(self):
        path = self.translate_path(self.path)
        if os.path.isdir(path):
            path = os.path.join(path, "index.html")
        if os.path.isfile(path) and path.endswith(".html"):
            with open(path, "r", encoding="utf-8") as f:
                html = f.read()
            html = html.replace("</body>", INJECT + "</body>")
            body = html.encode("utf-8")
            self.send_response(200)
            self.send_header("Content-Type", "text/html; charset=utf-8")
            self.send_header("Content-Length", str(len(body)))
            self.end_headers()
            self.wfile.write(body)
            return
        super().do_GET()


if __name__ == "__main__":
    port = int(sys.argv[1]) if len(sys.argv) > 1 else 8099
    server = http.server.ThreadingHTTPServer(("127.0.0.1", port), Handler)
    server.serve_forever()
