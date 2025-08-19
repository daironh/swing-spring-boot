# 🚀 Enterprise Git & Release Management Strategy for Finance/Banking

Managing releases in **regulated industries** like banking requires more than Git discipline:  
it requires **auditability, traceability, and alignment with Change Management processes (ServiceNow)**.  

This playbook describes a **branching strategy**, **versioning approach**, and **integration with ServiceNow** suitable for corporate banking environments.  

---

## 🌳 Branching Model Overview

We use **three main branches**:

- **`main`** → Only branch that pipelines deploy from (PRE & PROD). Always stable.  
- **`development`** → Ongoing feature development.  
- **`release/x.y.z`** → Temporary branch for stabilizing a version, preparing `pom.xml`, and QA iterations.  

Supporting branches:  
- **`feature/*`** → New functionality.  
- **`hotfix/*`** → Urgent fixes for PROD.  

---

## 🔢 Versioning (SemVer)

We follow **Semantic Versioning (SemVer)**:  

- **MAJOR** → Incompatible changes (`2.0.0`).  
- **MINOR** → Backward-compatible features (`1.3.0`).  
- **PATCH** → Bug fixes / hotfixes (`1.2.1`).  

Additionally, we use **pre-release labels** for QA and staging:  

- `1.2.0-SNAPSHOT` → Development snapshot.  
- `1.2.0-rc.1` → Release Candidate 1.  
- `1.2.0` → Final release.  

---

## 🎯 Tags — What and Why

A **Git tag** is a pointer to a specific commit, used to mark release points.  
- Tags provide **auditability** (which commit went to PROD).  
- Tags are **immutable references** (can't move without force).  
- In finance, tags are required for **traceability & compliance**.  

Examples:  
```bash
git tag -a v1.2.0-rc.1 -m "Release Candidate 1 for 1.2.0 - CHG1234567"
git tag -a v1.2.0 -m "Final Release 1.2.0 - CHG1234567"
```

---

## 🛠️ Workflow with Pipelines Deploying Only from `main`

### 1. Normal Development  
- Work happens in `development`.  
- Example `pom.xml`:  
```xml
<version>1.3.0-SNAPSHOT</version>
```

---

### 2. Cut a Release Branch  
- Create release branch from `development`:  
```bash
git checkout development
git checkout -b release/1.2.0
```
- Set version in `pom.xml`:  
```xml
<version>1.2.0-rc.1</version>
```

---

### 3. QA Iterations (Release Candidates)  
- QA bug fixes applied directly in `release/1.2.0`.  
- Example `pom.xml`:  
```xml
<version>1.2.0-rc.2</version>
```
- Tag each iteration:  
```bash
git tag -a v1.2.0-rc.2 -m "Release Candidate 2 for 1.2.0 - CHG1234567"
git push origin v1.2.0-rc.2
```

⚠️ **Pipelines can’t deploy from release** → after QA validation, **merge release → main** for PRE deployment:  
```bash
git checkout main
git merge --no-ff release/1.2.0
git push origin main
```

- Example `pom.xml` in `main` after merge (for PRE):  
```xml
<version>1.2.0-rc.2</version>
```

---

### 4. Final Release to PROD  
- Update final version in release branch:  
```xml
<version>1.2.0</version>
```
- Commit & tag:  
```bash
git commit -am "release: 1.2.0"
git tag -a v1.2.0 -m "Final Release 1.2.0 - CHG1234567"
git push origin v1.2.0
```
- Merge into `main` → triggers PROD deployment.  

---

### 5. Sync Back to Development  
After PROD:  
```bash
git checkout development
git merge --no-ff release/1.2.0
git push origin development
```
- Bump `pom.xml` to next dev cycle:  
```xml
<version>1.3.0-SNAPSHOT</version>
```

---

## 🔄 Hotfix Workflow  

If PROD (`1.2.0`) has a bug but `development` is already on `1.3.0`:  

1. Cut a hotfix branch from `main`:  
```bash
git checkout main
git checkout -b hotfix/1.2.1
```

2. Fix, bump version:  
```xml
<version>1.2.1-rc.1</version>
```
Tag:  
```bash
git tag -a v1.2.1-rc.1 -m "Hotfix Candidate 1 - CHG1237890"
```

3. Finalize release:  
```xml
<version>1.2.1</version>
```
```bash
git commit -am "hotfix: 1.2.1"
git tag -a v1.2.1 -m "Hotfix Release 1.2.1 - CHG1237890"
git push origin main --tags
```

4. Merge back into `development`:  
```bash
git checkout development
git merge --no-ff hotfix/1.2.1
git push origin development
```

---

## 📑 Release Notes Best Practices  

Every release must have a `RELEASE_NOTES.md`.  

### Template  
**Header**:  
- Release: `v1.2.0`  
- Change Ticket: `CHG1234567`  
- Release Manager: Jane Doe  
- Deployment Window: 2025-08-25 02:00 UTC  

**Scope**:  
- New AML reporting feature.  
- UI improvements.  

**Fixes**:  
- `FIX-3021`: Duplicate payment detection fixed.  
- `SEC-1842`: XSS patched.  

**Artifacts**:  
- Git Tag: `v1.2.0`  
- Commit: `abc1234`  
- Branch: `release/1.2.0`  

**Testing Evidence**:  
- QA report link.  
- UAT approval.  

**Rollback Plan**:  
- Restore to tag `v1.1.3`.  

---

## 🔗 ServiceNow Integration  

- **Each release/pre-release links to a SNOW Change Ticket**.  
- Best practice: include ticket in **tag message** & **release notes**.  
- Example:  
```bash
git tag -a v1.2.0 -m "Final Release 1.2.0 - Approved Change CHG1234567"
```

- In ServiceNow ticket, document:  
  - Git tag deployed.  
  - Link to `RELEASE_NOTES.md`.  
  - Test evidence & rollback plan.  

---

## ✅ DOs / ❌ DON’Ts  

✔️ **DO**:  
- Cut release branches from `development`.  
- Apply QA fixes directly to release branches.  
- Always merge releases/hotfixes back into `development`.  
- Always tag RCs and final releases.  
- Store standardized `RELEASE_NOTES.md`.  
- Link Git tags and SNOW tickets for audit.  

❌ **DON’T**:  
- Don’t deploy without a SNOW-approved change ticket.  
- Don’t patch `development` while QA stabilizes a release.  
- Don’t rebuild artifacts between PRE and PROD.  
- Don’t skip rollback planning.  

---

## 🎯 Summary  

This approach gives you:  
- **Compliance**: SNOW + Git traceability.  
- **Auditability**: Tags, release notes, ticket links.  
- **Control**: Release branches for QA stabilization.  
- **Flexibility**: Supports parallel dev, controlled hotfixes.  

Battle-tested for **finance and regulated industries** where stability, traceability, and governance are non-negotiable.  
