# Universidades Licenciadas - CRUD (Node.js + Express + CSV)

Aplicación web para gestionar (crear, leer, actualizar, eliminar y buscar) un listado
de universidades licenciadas. Los datos se guardan en un archivo **CSV editable**
(`data/universidades.csv`), no en una base de datos.

## Requisitos

- Node.js instalado (v16 o superior recomendado)

## Instalación

```bash
npm install
```

## Ejecutar

```bash
npm start
```

Luego abre en el navegador: **http://localhost:3000**

## Estructura del proyecto

```
app/
├── server.js              # Servidor Express y rutas (CRUD + búsqueda)
├── data-store.js           # Lógica de lectura/escritura del CSV
├── data/
│   └── universidades.csv    # "Base de datos" en CSV (se puede editar a mano)
├── views/
│   ├── index.ejs             # Listado + barra de búsqueda + tabla
│   └── form.ejs               # Formulario para crear/editar
└── public/
    └── styles.css              # Estilos (encabezado morado, tabla, botones)
```

## Funcionalidad

- **Listar**: `GET /` muestra la tabla con todas las universidades.
- **Buscar**: `GET /?q=texto` filtra por nombre, departamento, provincia o tipo
  de gestión. Se usa el método **GET**, por lo que la búsqueda queda en la URL
  (se puede copiar/compartir/recargar).
- **Ordenar**: clic en cualquier encabezado de columna (usa `GET` con `sort` y `dir`).
- **Crear**: botón "+ Nueva universidad" → formulario → `POST /universidades`.
- **Editar**: botón "Editar" en cada fila → formulario prellenado → `POST /editar/:id`.
- **Eliminar**: botón "Eliminar" en cada fila → `POST /eliminar/:id` (con confirmación).

## Campos del formulario

| Campo | Tipo | Notas |
|---|---|---|
| Nombre de la Universidad | texto | obligatorio |
| Fecha de Licenciamiento | fecha | se guarda como `yyyy-mm-dd`, se muestra `dd-mm-yyyy` |
| Resolución | número 1 a 5 | se muestra como estrellas ★ en la tabla |
| Departamento | texto | se muestra combinado como "Departamento/Provincia" |
| Provincia | texto | |
| Tipo de Gestión | Pública / Privada | |

## Editar el CSV directamente

Como los datos viven en `data/universidades.csv`, también puedes abrirlo y
editarlo a mano con Excel, Google Sheets o un editor de texto — los cambios se
reflejarán la próxima vez que la app lea el archivo (por ejemplo, al recargar
la página).

## Notas

- No se incluye `node_modules/` (se genera con `npm install`).
- Si cambias el puerto, usa la variable de entorno `PORT` (por defecto 3000).
