# Collectiverse

Proyecto web desarrollado con Django para una plataforma de venta e intercambio de cartas y productos relacionados con el mundo otaku.

## Integrantes

- Joaquin Torres
- Franco Rojas
- Sebastian David
- Joel Choquepata
  
## Estado actual

Hasta ahora se ha configurado la base del proyecto Django y se ha definido el modelo de datos inicial.

### Tecnologías

- Python
- Django 4.0.5
- SQLite3
- Pillow
- Entorno virtual (`venv`)

## Estructura principal

```text
Collectiverse/
├── my_env/
├── requirements.txt
└── CollectiverseProject/
    ├── manage.py
    ├── CollectiverseProject/
    │   ├── settings.py
    │   ├── urls.py
    │   ├── asgi.py
    │   └── wsgi.py
    └── MyWebApps/
        ├── __init__.py
        └── Collectiverse/
            ├── admin.py
            ├── apps.py
            ├── models/
            │   ├── __init__.py
            │   ├── product.py
            │   ├── publication.py
            │   └── rating.py
            └── migrations/
```

## Usuarios

No se creó un modelo de usuario propio.

Se utiliza el sistema de autenticación que Django proporciona mediante:

```text
django.contrib.auth
```

Los usuarios se administran desde:

```text
Admin → Authorization → Users
```

También se planteó utilizar grupos de Django para diferenciar:

- `buyer`
- `seller`
- `trader`

## Modelos propios

La aplicación tiene tres modelos principales:

### Product

Tabla:

```text
products
```

Campos principales:

```text
id_product
name
product_type
image
quantity
price
status
created
modified
created_id
modified_id
```

### Publication

Tabla:

```text
publications
```

Campos principales:

```text
id_publication
user_id
product_id
publication_type
status
created
modified
created_id
modified_id
```

### Rating

Tabla:

```text
ratings
```

Campos principales:

```text
id_rating
user_id
publication_id
user_rating
publication_rating
status
created
modified
created_id
modified_id
```

## Relaciones

```text
User 1 ───── N Publications

Product 1 ── N Publications

User 1 ───── N Ratings

Publication 1 ── N Ratings
```

## Convenciones utilizadas

Se decidió utilizar:

- nombres en inglés;
- `snake_case` para los atributos;
- nombres de tablas en plural;
- `db_table` para definir explícitamente el nombre de las tablas.

Ejemplos:

```text
products
publications
ratings
```

## Campos comunes

Las tablas propias incluyen:

```text
status       → Boolean
created      → DateTime
modified     → DateTime
created_id   → Integer
modified_id  → Integer
```

Actualmente:

- `status` tiene valor por defecto `True`.
- `created` se genera automáticamente al crear el registro.
- `modified` se actualiza automáticamente al modificar el registro.
- `created_id` es obligatorio.
- `modified_id` puede ser `NULL`.

## Migraciones

Para crear y actualizar las tablas se utilizan:

```bash
python manage.py makemigrations
python manage.py migrate
```

## Superusuario

El usuario administrador se crea con:

```bash
python manage.py createsuperuser
```

Y el panel de administración está disponible en:

```text
http://127.0.0.1:8000/admin/
```

## Ejecutar el proyecto

Activar el entorno virtual:

```cmd
my_env\Scripts\activate
```

Ejecutar el servidor:

```bash
python manage.py runserver
```

## Base de datos

Se utiliza:

```text
SQLite3
```

La base de datos se encuentra en:

```text
db.sqlite3
```

La base de datos fue reiniciada para aplicar correctamente la nueva estructura de los modelos.

## Próximos pasos

- Configurar completamente `created_id` y `modified_id`.
- Configurar el Django Admin.
- Crear grupos `buyer`, `seller` y `trader`.
- Crear registros de prueba.
- Verificar las relaciones y restricciones.
- Continuar con el informe del laboratorio.
