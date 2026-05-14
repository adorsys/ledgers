#!/usr/bin/env node
/**
 * Uniqus Mythos-Aware Code Review — CI agent
 *
 * Runs inside a CI pipeline (GitHub Actions, GitLab CI, Jenkins, ...)
 * and submits the changed files in the current commit/PR to the
 * Mythos backend, then:
 *   - prints a markdown summary (used as PR-comment body),
 *   - posts the run to /v1/ci-report so it shows up in Pillar 02,
 *   - exits non-zero when any 'critical' finding is returned (gates merge).
 *
 * Configuration (env vars):
 *   MYTHOS_API_URL     Mythos backend base URL (e.g. https://mythos-aware-assessment.onrender.com)
 *   MYTHOS_INDUSTRY    "banking" | "pharma" | "healthcare" | "energy"  (default: banking)
 *   CI_REPORT_SECRET   Optional shared secret. Must match backend's CI_REPORT_SECRET when set.
 *   FAIL_ON            critical | high | medium | low | never    (default: critical)
 *   MAX_FILES          Cap on files scanned per run (default: 25)
 *   MAX_FILE_BYTES     Skip files larger than this (default: 200000 = 200KB)
 *   SCAN_BASE_REF      Base ref for diff (default: origin/main)
 *   SCAN_ALL           "1" to scan every code file in the repo, ignoring the diff (default: 0)
 *
 * GitHub-Actions-provided vars consumed automatically:
 *   GITHUB_REPOSITORY  e.g. "owner/repo"
 *   GITHUB_REF_NAME    branch name
 *   GITHUB_SHA         commit SHA
 *   GITHUB_EVENT_NAME  pull_request | push | ...
 *   GITHUB_EVENT_PATH  path to the workflow event JSON (used to extract PR number / URLs)
 *   GITHUB_SERVER_URL  defaults to https://github.com
 *   GITHUB_ACTOR       triggering user
 *
 * Outputs:
 *   - stdout: human-readable summary
 *   - mythos-report.md   markdown PR-comment body
 *   - mythos-report.json full findings payload (workflow artefact)
 */

"use strict";

const fs   = require("fs");
const path = require("path");
const cp   = require("child_process");

// ───────────────────────── config ─────────────────────────
const CFG = {
  apiUrl:       process.env.MYTHOS_API_URL || "https://mythos-aware-assessment.onrender.com",
  industry:     process.env.MYTHOS_INDUSTRY || "banking",
  secret:       process.env.CI_REPORT_SECRET || "",
  failOn:       (process.env.FAIL_ON || "critical").toLowerCase(),
  maxFiles:     Number(process.env.MAX_FILES || 25),
  maxFileBytes: Number(process.env.MAX_FILE_BYTES || 200_000),
  baseRef:      process.env.SCAN_BASE_REF || "origin/main",
  scanAll:      process.env.SCAN_ALL === "1",
};

// Extensions worth reviewing (mirror the backend's tree filter)
const CODE_EXTS = new Set([
  "py","js","ts","tsx","jsx","mjs","cjs","java","kt","kts","go","rb","cs",
  "cpp","cc","cxx","c","h","hpp","hxx","php","rs","swift","scala","m","mm",
  "pl","sh","bash","zsh","ps1","sql","groovy","r","lua","dart","ex","exs",
  "yml","yaml","tf","hcl","json","xml","toml","properties",
  "html","htm","css","scss",
]);
const NO_EXT_NAMES = new Set(["dockerfile","makefile","gemfile","procfile","rakefile","jenkinsfile"]);
const NOISE_RE = /\.(min|bundle|map|lock)\.|package-lock\.json|yarn\.lock|pnpm-lock\.yaml/i;
const SKIP_PREFIXES = ["node_modules/",".git/","dist/","build/","vendor/","__pycache__/",".next/",".venv/","venv/"];

// ───────────────────────── helpers ─────────────────────────
function isCodeFile(p) {
  const lower = p.toLowerCase();
  if (SKIP_PREFIXES.some(pre => lower.startsWith(pre) || lower.includes("/" + pre))) return false;
  if (NOISE_RE.test(lower)) return false;
  const base = path.basename(p);
  if (NO_EXT_NAMES.has(base.toLowerCase())) return true;
  const ext = (base.split(".").pop() || "").toLowerCase();
  if (!base.includes(".")) return false;
  return CODE_EXTS.has(ext);
}

function sh(cmd, args) {
  const r = cp.spawnSync(cmd, args, { encoding: "utf-8" });
  if (r.status !== 0) {
    process.stderr.write(`(non-fatal) ${cmd} ${args.join(" ")} → ${r.status}\n${r.stderr}\n`);
    return "";
  }
  return r.stdout;
}

function readGitHubEvent() {
  const p = process.env.GITHUB_EVENT_PATH;
  if (!p || !fs.existsSync(p)) return {};
  try { return JSON.parse(fs.readFileSync(p, "utf-8")); }
  catch { return {}; }
}

function discoverChangedFiles() {
  if (CFG.scanAll) {
    // tracked code files
    const out = sh("git", ["ls-files"]);
    return out.split("\n").filter(Boolean).filter(isCodeFile);
  }

  // Determine the diff base
  let base = CFG.baseRef;
  const evt = readGitHubEvent();
  if (process.env.GITHUB_EVENT_NAME === "pull_request" && evt.pull_request) {
    // GH Actions checks out a merge ref; the PR object has the base SHA.
    base = evt.pull_request.base && evt.pull_request.base.sha
      ? evt.pull_request.base.sha
      : ("origin/" + (evt.pull_request.base && evt.pull_request.base.ref || "main"));
  }

  // Make sure we have the base ref locally
  if (/^origin\//.test(base)) {
    sh("git", ["fetch", "--depth=50", "origin", base.replace(/^origin\//, "")]);
  }

  const out = sh("git", ["diff", "--name-only", "--diff-filter=AMR", base + "...HEAD"]);
  let files = out.split("\n").filter(Boolean).filter(isCodeFile);

  // Fallback for first commit / shallow checkouts where diff is empty
  if (files.length === 0) {
    const fallback = sh("git", ["diff-tree", "--no-commit-id", "--name-only", "-r", "HEAD"]);
    files = fallback.split("\n").filter(Boolean).filter(isCodeFile);
  }
  return files;
}

function inferLanguage(filename) {
  const ext = (filename.split(".").pop() || "").toLowerCase();
  return ({
    py: "python", js: "javascript", ts: "typescript", tsx: "typescript", jsx: "javascript",
    java: "java", kt: "kotlin", go: "go", rb: "ruby", cs: "csharp",
    cpp: "cpp", cc: "cpp", c: "c", h: "c", php: "php", rs: "rust", swift: "swift",
    scala: "scala", sh: "bash", sql: "sql", yml: "yaml", yaml: "yaml",
    tf: "terraform", hcl: "terraform", json: "json", xml: "xml",
    html: "html", css: "css",
  })[ext] || "other";
}

async function postJSON(url, body, extraHeaders) {
  const headers = Object.assign(
    { "Content-Type": "application/json", "User-Agent": "uniqus-mythos-ci/1.0" },
    extraHeaders || {}
  );
  const res = await fetch(url, { method: "POST", headers, body: JSON.stringify(body) });
  const text = await res.text();
  let data; try { data = JSON.parse(text); } catch { data = { raw: text }; }
  return { ok: res.ok, status: res.status, data };
}

async function reviewOne(filename, code) {
  return postJSON(CFG.apiUrl + "/v1/mythos-review", {
    code, filename, language: inferLanguage(filename), industry: CFG.industry,
  });
}

function sevWeight(s) {
  return ({ critical: 4, high: 3, medium: 2, low: 1 })[s] || 0;
}

function shouldFail(counts) {
  const order = ["low","medium","high","critical"];
  const idx = order.indexOf(CFG.failOn);
  if (idx < 0) return false; // "never"
  for (let i = idx; i < order.length; i++) {
    if ((counts[order[i]] || 0) > 0) return true;
  }
  return false;
}

function severityChip(s) {
  const m = { critical: "[CRITICAL]", high: "[HIGH]", medium: "[MEDIUM]", low: "[LOW]" };
  return m[s] || "[INFO]";
}

function buildMarkdown(rollup, ctx) {
  const c = rollup.counts;
  const total = c.critical + c.high + c.medium + c.low;
  const ind = CFG.industry.charAt(0).toUpperCase() + CFG.industry.slice(1);
  let md = "";
  md += "## Uniqus Mythos-Aware Code Review\n\n";
  md += "_" + ind + "-tuned threat library. " + rollup.scanned + " file(s) scanned, " +
        total + " finding(s) detected._\n\n";
  md += "| Severity | Count |\n|---|---|\n";
  md += "| Critical | " + c.critical + " |\n";
  md += "| High     | " + c.high     + " |\n";
  md += "| Medium   | " + c.medium   + " |\n";
  md += "| Low      | " + c.low      + " |\n\n";

  if (rollup.findings.length === 0) {
    md += "No findings. The pipeline is clean against the " + ind + "-tuned threat library.\n";
  } else {
    md += "### Top findings\n\n";
    const top = rollup.findings
      .slice()
      .sort((a, b) => sevWeight(b.severity) - sevWeight(a.severity))
      .slice(0, 10);
    for (const f of top) {
      md += "- " + severityChip(f.severity) + " **" + (f.title || f.id) + "** — `" +
            (f.location || f.filename || "?") + "`";
      if (Array.isArray(f.regs) && f.regs.length) {
        md += "  \n  _Regs:_ " + f.regs.slice(0, 4).join(", ");
      }
      md += "\n";
    }
    if (rollup.findings.length > 10) {
      md += "\n_+" + (rollup.findings.length - 10) + " more — see workflow artefacts._\n";
    }
  }
  md += "\n[Live view in Mythos portal →](" + CFG.apiUrl.replace(/\/$/, "") + ")\n";
  return md;
}

async function postCIReport(rollup, ctx) {
  const payload = {
    repo:      ctx.repo,
    pr:        ctx.pr,
    sha:       ctx.sha,
    branch:    ctx.branch,
    commitUrl: ctx.commitUrl,
    prUrl:     ctx.prUrl,
    actor:     ctx.actor,
    source:    ctx.source,
    industry:  CFG.industry,
    files:     rollup.files,
    findings:  rollup.findings,
    summary:   rollup.counts,
  };
  const headers = CFG.secret ? { "x-ci-secret": CFG.secret } : {};
  const r = await postJSON(CFG.apiUrl + "/v1/ci-report", payload, headers);
  if (!r.ok) {
    process.stderr.write("ci-report POST failed: " + r.status + " " + JSON.stringify(r.data).slice(0, 500) + "\n");
  } else {
    process.stdout.write("ci-report recorded (" + r.data.recorded + " findings).\n");
  }
}

// ───────────────────────── main ─────────────────────────
(async function main() {
  // Build run context from GH Actions env (works for other CIs too via
  // generic env mapping)
  const evt = readGitHubEvent();
  const ctx = {
    repo:      process.env.GITHUB_REPOSITORY || sh("git", ["config","--get","remote.origin.url"]).trim().replace(/.*github\.com[:/]/,"").replace(/\.git$/, ""),
    sha:       (process.env.GITHUB_SHA || sh("git", ["rev-parse","HEAD"]).trim()).slice(0, 40),
    branch:    process.env.GITHUB_REF_NAME || sh("git", ["rev-parse","--abbrev-ref","HEAD"]).trim(),
    actor:     process.env.GITHUB_ACTOR || "ci",
    source:    process.env.GITHUB_ACTIONS ? "GitHub Actions"
             : process.env.GITLAB_CI       ? "GitLab CI"
             : process.env.JENKINS_URL     ? "Jenkins"
             : "CI Pipeline",
    pr:        "",
    prUrl:     "",
    commitUrl: "",
  };
  if (process.env.GITHUB_EVENT_NAME === "pull_request" && evt.pull_request) {
    ctx.pr     = String(evt.pull_request.number || "");
    ctx.prUrl  = evt.pull_request.html_url || "";
  }
  const serverUrl = process.env.GITHUB_SERVER_URL || "https://github.com";
  if (ctx.repo && ctx.sha) ctx.commitUrl = serverUrl + "/" + ctx.repo + "/commit/" + ctx.sha;

  process.stdout.write("Mythos CI scan: " + ctx.repo + " " + (ctx.pr ? "PR #"+ctx.pr : ctx.branch) + " @ " + ctx.sha.slice(0,7) + "\n");
  process.stdout.write("Industry: " + CFG.industry + " · backend: " + CFG.apiUrl + "\n");

  // Discover files
  const all = discoverChangedFiles();
  const files = all.slice(0, CFG.maxFiles);
  if (all.length > CFG.maxFiles) {
    process.stdout.write("(capping " + all.length + " changed files → " + CFG.maxFiles + " scanned)\n");
  }
  if (files.length === 0) {
    process.stdout.write("No code files changed. Skipping review.\n");
    fs.writeFileSync("mythos-report.md", "## Uniqus Mythos-Aware Code Review\n\nNo code files changed in this run.\n");
    process.exit(0);
  }

  // Scan each file
  const rollup = { scanned: 0, files: [], findings: [], counts: { critical: 0, high: 0, medium: 0, low: 0 } };
  for (const f of files) {
    let code;
    try {
      const stat = fs.statSync(f);
      if (stat.size > CFG.maxFileBytes) {
        process.stdout.write("[skip] " + f + " (" + stat.size + "B > maxFileBytes)\n");
        continue;
      }
      code = fs.readFileSync(f, "utf-8");
    } catch (err) {
      process.stdout.write("[skip] " + f + " (unreadable: " + err.message + ")\n");
      continue;
    }
    process.stdout.write("[scan] " + f + " (" + code.length + "B) ... ");
    const r = await reviewOne(f, code);
    if (!r.ok) {
      process.stdout.write("ERROR " + r.status + " " + JSON.stringify(r.data).slice(0, 200) + "\n");
      continue;
    }
    const fnd = Array.isArray(r.data.findings) ? r.data.findings : [];
    rollup.scanned++;
    rollup.files.push(f);
    for (const finding of fnd) {
      finding.filename = finding.filename || f;
      rollup.findings.push(finding);
      const s = finding.severity || "low";
      if (rollup.counts[s] !== undefined) rollup.counts[s]++;
    }
    process.stdout.write(fnd.length + " finding(s)\n");
  }

  // Print + persist report
  const md = buildMarkdown(rollup, ctx);
  fs.writeFileSync("mythos-report.md", md);
  fs.writeFileSync("mythos-report.json", JSON.stringify({ ctx, rollup }, null, 2));
  process.stdout.write("\n" + md + "\n");

  // Push to portal
  try { await postCIReport(rollup, ctx); } catch (err) {
    process.stderr.write("ci-report exception: " + err.message + "\n");
  }

  // Gate
  if (shouldFail(rollup.counts)) {
    const order = ["critical","high","medium","low"];
    const blocker = order.find(s => (rollup.counts[s] || 0) > 0 && sevWeight(s) >= sevWeight(CFG.failOn));
    process.stderr.write("\nFAIL: " + rollup.counts[blocker] + " " + blocker + " finding(s) — failing the build (FAIL_ON=" + CFG.failOn + ").\n");
    process.exit(1);
  }

  process.stdout.write("\nPASS: no findings at or above '" + CFG.failOn + "' severity.\n");
  process.exit(0);
})().catch(err => {
  process.stderr.write("FATAL: " + (err && err.stack || err) + "\n");
  process.exit(2);
});
