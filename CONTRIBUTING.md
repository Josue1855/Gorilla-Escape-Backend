# Flujo obligatorio de desarrollo

Decisión explícita del Product Owner, 2026-10-16. La rama por defecto y base de integración de este repositorio es **develop**.

1. Actualizar develop y crear una rama de tarea desde esa base.
2. Implementar en la rama de tarea; no hacer commits directamente en develop ni main.
3. Abrir PR hacia develop con alcance, criterios de aceptación, pruebas y evidencia.
4. Integrar a develop después de revisión y checks aplicables; comprobar la integración.
5. Cuando la entrega completa esté validada y aprobada, abrir PR de promoción develop → main. main conserva exclusivamente entregas; no hay merge automático.

Ejemplo de preparación de una tarea:

```bash
git fetch origin
git switch develop
git pull --ff-only origin develop
git switch -c codex/nombre-de-tarea
```

No hacer push directo a main, force push ni cambios de historia. Si falta validación, la entrega sigue en develop/ramas de tarea. La implementación de runtime, tests y CI continúa planificada; esta regla no equivale a tenerlos implementados ni aprobados.
