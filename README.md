# Sistema de Inventario — Java Swing + MongoDB

Proyecto Maven para NetBeans, basado en el documento «Sistema_Inventario poo».

## Requisitos
- JDK 21
- NetBeans con soporte Maven
- MongoDB Server local activo en `localhost:27017`
- Conexión a Internet la primera vez para descargar la dependencia Maven de MongoDB

## Inicio rápido
1. Descomprime el ZIP y abre la carpeta `Inventario_NetBeans` en NetBeans como proyecto Maven.
2. Confirma que MongoDB Server esté encendido. Compass es opcional para inspeccionar los datos.
3. Configura la variable de entorno `INVENTARIO_DEMO_PASSWORD` con la contraseña de prueba elegida antes de abrir NetBeans. En NetBeans, ejecuta **com.inventario.Inicializar** una sola vez. Esto crea seis productos y el usuario de demostración. **No necesitas instalar mongosh para este paso.**
4. Ejecuta **com.inventario.Main**.
5. Ingresa con usuario `demostracion` y la contraseña de prueba elegida.
6. Cambia o elimina la cuenta de demostración antes de utilizar datos reales.

También puedes ejecutar desde la raíz del proyecto con Maven:
```
mvn compile exec:java -Dexec.mainClass=com.inventario.Inicializar
mvn compile exec:java -Dexec.mainClass=com.inventario.Main
```

## Funcionalidades
- Inicio de sesión con contraseñas PBKDF2-HMAC-SHA256 y sal aleatoria
- Registro, edición y eliminación de productos
- Código único como `_id` de MongoDB
- Búsqueda por código/nombre y filtro por categoría
- Ordenamiento al pulsar encabezados de la tabla
- Entradas y salidas atómicas con validación de stock
- Reporte de agotados, bajo mínimo y valor total
- Cierre de sesión
- Persistencia en base `inventario_g5_gp2`, colección `productos` y `usuarios`

## Configuración opcional
Variables de entorno `MONGO_URI` y `MONGO_DB` para cambiar servidor y base.

## Notas importantes
- El archivo `mongodb/inicializar.js` es una alternativa opcional para sembrar productos; el método recomendado es ejecutar la clase Java `Inicializar`, que además crea el usuario seguro.
- Este código es una implementación nueva basada en el documento anterior.
- Se verificó la conexión y la inicialización contra MongoDB Atlas.
- El diagrama UML del Word es conceptual y contiene clases adicionales. Para un UML exacto del código, representa `Main`, `Producto`, `Repositorio`, `Autenticacion` e `Inicializar`.
- Los movimientos modifican las existencias; esta versión no guarda un historial individual de cada movimiento.

Para Atlas, copia la URI en **Connect > Drivers** y configura `MONGO_URI` en el
entorno desde el que abres NetBeans o Maven. Usa este formato como referencia:

```text
mongodb+srv://<usuario>:<password>@<cluster>.mongodb.net/?retryWrites=true&w=majority
```
