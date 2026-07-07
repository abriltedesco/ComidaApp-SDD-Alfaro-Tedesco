#!/usr/bin/env python3
"""
Script para crear issues en GitHub a partir de tasks.md
Lee las tareas T001-T081 y crea un issue por cada una con:
- Título: [T0XX] Título de la tarea
- Body: descripción completa con archivos, FRs, dependencias
- Labels: fase-XX, backend/frontend, categoría, estado
"""

import re
import subprocess
import json
import sys
import os

TASKS_FILE = os.path.join(os.path.dirname(__file__), "..", "specs", "001-comidapp-mvp", "tasks.md")
REPO = "Instituto-Politecnico-Modelo/2026-MTN-TP5--ComidApp-SDD"
GH_BIN = os.path.expanduser("~/bin/gh")

# ── Mapeo de tareas a labels de categoría ────────────────────────────────────
BACKEND_TASKS = set(range(1, 50))   # T001-T049
FRONTEND_TASKS = set(range(50, 82)) # T050-T081

DOMINIO_TASKS = {4, 5, 6, 7, 8, 9}
INFRA_TASKS = {10, 11, 12, 13, 14, 15, 16, 23, 24, 36}
USECASE_TASKS = {17, 18, 19, 20, 21, 22}
API_TASKS = {25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35}
TEST_TASKS = set(range(37, 50))
SETUP_TASKS = {1, 2, 3, 50, 51, 52}
INTEGRATION_FE_BE = set(range(70, 79))
E2E_TASKS = {79, 80, 81}

PARTIAL_TASKS = {53, 54, 55, 56, 57, 59, 60, 61, 62, 63, 65}


def get_category_labels(task_num):
    """Determina los labels de categoría para una tarea."""
    labels = []
    if task_num in BACKEND_TASKS:
        labels.append("backend")
    if task_num in FRONTEND_TASKS:
        labels.append("frontend")
    if task_num in DOMINIO_TASKS:
        labels.append("dominio")
    if task_num in INFRA_TASKS:
        labels.append("infraestructura")
    if task_num in API_TASKS:
        labels.append("api")
    if task_num in TEST_TASKS:
        labels.append("tests")
    if task_num in SETUP_TASKS:
        labels.append("setup")
    if task_num in INTEGRATION_FE_BE:
        labels.append("integracion-fe-be")
    if task_num in E2E_TASKS:
        labels.append("e2e")
    if task_num in PARTIAL_TASKS:
        labels.append("parcial")
    else:
        labels.append("pendiente")
    return labels


def parse_tasks(filepath):
    """Parsea tasks.md y extrae la info de cada tarea T001-T081."""
    with open(filepath, "r", encoding="utf-8") as f:
        content = f.read()

    # Patrón para encontrar cada tarea: #### T0XX — Título
    # Captura todo hasta el próximo #### T o hasta ## (nueva sección)
    pattern = r'####\s+(T\d{3})\s*—\s*(.+?)(?:\s*\[(?:P|PARTIAL|DONE)\])?\s*\n(.*?)(?=\n####\s+T\d{3}|\n##\s|\n---\n## Tabla Resumen|\Z)'
    matches = re.findall(pattern, content, re.DOTALL)

    tasks = []
    for task_id, title, body_raw in matches:
        title = title.strip().rstrip(" [P]").rstrip(" [PARTIAL]").rstrip(" [DONE]")
        # Limpiar marcas residuales del título
        title = re.sub(r'\s*\[(?:P|PARTIAL|DONE)\]\s*$', '', title).strip()

        # Extraer campos del body
        archivos = ""
        fr = ""
        depende = ""
        descripcion = ""

        for line in body_raw.strip().split("\n"):
            line_stripped = line.strip()
            if line_stripped.startswith("- **Archivos**:"):
                archivos = line_stripped.replace("- **Archivos**:", "").strip()
            elif line_stripped.startswith("- **FR**:"):
                fr = line_stripped.replace("- **FR**:", "").strip()
            elif line_stripped.startswith("- **Depende de**:"):
                depende = line_stripped.replace("- **Depende de**:", "").strip()
            elif line_stripped.startswith("- **Descripción**:"):
                descripcion = line_stripped.replace("- **Descripción**:", "").strip()

        # Si la descripción es multilínea, capturar todo lo que sigue a "Descripción:"
        desc_match = re.search(r'\*\*Descripción\*\*:\s*(.+?)(?=\n---|\n####|\Z)', body_raw, re.DOTALL)
        if desc_match:
            descripcion = desc_match.group(1).strip()

        task_num = int(task_id[1:])

        # Determinar fase desde la tabla resumen
        fase = get_fase(task_num)

        tasks.append({
            "id": task_id,
            "num": task_num,
            "title": title,
            "archivos": archivos,
            "fr": fr,
            "depende": depende,
            "descripcion": descripcion,
            "fase": fase,
            "body_raw": body_raw.strip()
        })

    return tasks


def get_fase(num):
    """Devuelve la fase según el número de tarea."""
    if num <= 3:
        return 1
    elif num <= 9:
        return 2
    elif num <= 11:
        return 3
    elif num <= 16:
        return 4
    elif num <= 22:
        return 5
    elif num <= 24:
        return 6
    elif num <= 36:
        return 7
    elif num <= 40:
        return 8
    elif num <= 44:
        return 9
    elif num <= 49:
        return 10
    elif num <= 52:
        return 11
    elif num <= 62:
        return 12
    elif num <= 69:
        return 13
    elif num <= 78:
        return 14
    else:
        return 15


def build_issue_body(task):
    """Construye el body markdown del issue."""
    lines = []
    lines.append(f"## {task['id']} — {task['title']}")
    lines.append("")

    if task["archivos"]:
        lines.append(f"**Archivos**: {task['archivos']}")
    if task["fr"]:
        lines.append(f"**FRs asociados**: {task['fr']}")
    if task["depende"]:
        lines.append(f"**Depende de**: {task['depende']}")

    lines.append(f"**Fase**: {task['fase']}")
    lines.append("")

    lines.append("### Descripción")
    lines.append("")
    lines.append(task["descripcion"] if task["descripcion"] else task["body_raw"])
    lines.append("")

    lines.append("---")
    lines.append(f"*Generado automáticamente desde `specs/001-comidapp-mvp/tasks.md`*")

    return "\n".join(lines)


def get_existing_issues():
    """Obtiene los títulos de issues existentes para evitar duplicados."""
    result = subprocess.run(
        [GH_BIN, "issue", "list", "--repo", REPO, "--limit", "200", "--state", "all", "--json", "title"],
        capture_output=True, text=True
    )
    if result.returncode != 0:
        print(f"WARN: no se pudieron listar issues existentes: {result.stderr}")
        return set()

    issues = json.loads(result.stdout)
    return {i["title"] for i in issues}


def create_issue(task, existing_titles):
    """Crea un issue en GitHub para una tarea."""
    issue_title = f"[{task['id']}] {task['title']}"

    # Evitar duplicados
    if issue_title in existing_titles:
        print(f"  SKIP {task['id']}: ya existe")
        return True

    body = build_issue_body(task)
    fase_label = f"fase-{task['fase']:02d}"
    labels = [fase_label] + get_category_labels(task["num"])
    labels_str = ",".join(labels)

    cmd = [
        GH_BIN, "issue", "create",
        "--repo", REPO,
        "--title", issue_title,
        "--body", body,
        "--label", labels_str
    ]

    result = subprocess.run(cmd, capture_output=True, text=True)
    if result.returncode != 0:
        print(f"  ERROR {task['id']}: {result.stderr.strip()}")
        return False
    else:
        url = result.stdout.strip()
        print(f"  OK {task['id']}: {url}")
        return True


def main():
    print(f"=== Creando issues desde tasks.md ===")
    print(f"Repo: {REPO}")
    print()

    # Parsear tareas
    tasks = parse_tasks(TASKS_FILE)
    print(f"Tareas encontradas: {len(tasks)}")

    if len(tasks) == 0:
        print("ERROR: no se encontraron tareas. Verificar el formato de tasks.md")
        sys.exit(1)

    # Obtener issues existentes
    print("Verificando issues existentes...")
    existing = get_existing_issues()
    print(f"Issues existentes: {len(existing)}")
    print()

    # Crear issues
    ok = 0
    skip = 0
    fail = 0

    for task in tasks:
        issue_title = f"[{task['id']}] {task['title']}"
        if issue_title in existing:
            print(f"  SKIP {task['id']}: ya existe")
            skip += 1
        else:
            success = create_issue(task, existing)
            if success:
                ok += 1
            else:
                fail += 1

    print()
    print(f"=== Resumen ===")
    print(f"  Creados: {ok}")
    print(f"  Saltados (duplicado): {skip}")
    print(f"  Fallidos: {fail}")
    print(f"  Total procesados: {ok + skip + fail}")


if __name__ == "__main__":
    main()
