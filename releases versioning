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
