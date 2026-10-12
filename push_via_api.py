#!/usr/bin/env python3
"""Push local HEAD to GitHub via the Git Database API (no git push auth needed)."""
import base64, json, subprocess, sys, urllib.request
sys.path.insert(0, "/opt/hatch/skills/skill-creator/bin")
from dynamic_credentials import add_surrogate_to_request, read_json_response

OWNER, REPO, BRANCH = "kulana121-bit", "new-file-manager", "main"
HOSTS = ("api.github.com",)

def call(method, path, data=None):
    req = urllib.request.Request(
        "https://api.github.com" + path,
        data=json.dumps(data).encode() if data is not None else None,
        method=method, headers={"Accept": "application/vnd.github+json"})
    add_surrogate_to_request(req, "custom.github", allowed_hosts=HOSTS)
    return read_json_response(urllib.request.urlopen(req))

def sh(cmd):
    return subprocess.run(cmd, shell=True, capture_output=True, text=True, check=True).stdout

# current remote HEAD
ref = call("GET", f"/repos/{OWNER}/{REPO}/git/refs/heads/{BRANCH}")
parent_sha = ref["object"]["sha"]
print("remote HEAD:", parent_sha)

# all tracked files in local HEAD
files = sh("git ls-tree -r HEAD --name-only").split()
print("files:", len(files))

tree = []
for i, path in enumerate(files):
    mode = sh(f"git ls-tree HEAD -- {path}").split()[0]
    content = subprocess.run(f"git show HEAD:{path}", shell=True, capture_output=True).stdout
    blob = call("POST", f"/repos/{OWNER}/{REPO}/git/blobs",
                {"content": base64.b64encode(content).decode(), "encoding": "base64"})
    tree.append({"path": path, "mode": mode, "type": "blob", "sha": blob["sha"]})
    if (i + 1) % 10 == 0:
        print(f"  blobs {i+1}/{len(files)}")

new_tree = call("POST", f"/repos/{OWNER}/{REPO}/git/trees", {"tree": tree})
print("tree:", new_tree["sha"][:8])

msg = sh("git log -1 --format=%B HEAD").strip()
commit = call("POST", f"/repos/{OWNER}/{REPO}/git/commits",
              {"message": msg, "tree": new_tree["sha"], "parents": [parent_sha]})
print("commit:", commit["sha"][:8])

call("PATCH", f"/repos/{OWNER}/{REPO}/git/refs/heads/{BRANCH}", {"sha": commit["sha"]})
print("PUSHED OK ->", commit["sha"])
