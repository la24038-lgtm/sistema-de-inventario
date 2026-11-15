// Ejecutar desde una terminal del sistema con: mongosh --file mongodb/inicializar.js
// Este script crea los productos; el usuario con hash PBKDF2 se crea con com.inventario.Inicializar.
const base = db.getSiblingDB('inventario_g5_gp2');
const productos = [
  {_id:'P001',nombre:'Laptop',descripcion:'Equipo portátil',categoria:'Tecnología',precio:650.0,cantidad:8,stockMinimo:2},
  {_id:'P002',nombre:'Mouse',descripcion:'Mouse USB',categoria:'Tecnología',precio:12.5,cantidad:30,stockMinimo:5},
  {_id:'P003',nombre:'Teclado',descripcion:'Teclado USB',categoria:'Tecnología',precio:22.0,cantidad:12,stockMinimo:3},
  {_id:'P004',nombre:'Cuaderno',descripcion:'Cuaderno rayado',categoria:'Papelería',precio:2.5,cantidad:40,stockMinimo:10},
  {_id:'P005',nombre:'Bolígrafo',descripcion:'Tinta azul',categoria:'Papelería',precio:0.75,cantidad:60,stockMinimo:15},
  {_id:'P006',nombre:'Monitor',descripcion:'Monitor LED',categoria:'Tecnología',precio:180.0,cantidad:4,stockMinimo:2}
];
for (const p of productos) base.productos.updateOne({_id:p._id}, {$setOnInsert:p}, {upsert:true});
print('Productos de demostración preparados. Para crear usuario ejecutar com.inventario.Inicializar desde NetBeans.');
