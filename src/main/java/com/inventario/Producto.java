package com.inventario;

import org.bson.Document;

public record Producto(String codigo, String nombre, String descripcion, String categoria,
                       double precio, int cantidad, int stockMinimo) {
    public Producto {
        if (codigo == null || codigo.isBlank() || nombre == null || nombre.isBlank() ||
            categoria == null || categoria.isBlank()) throw new IllegalArgumentException("Código, nombre y categoría son obligatorios.");
        if (!Double.isFinite(precio) || precio < 0 || cantidad < 0 || stockMinimo < 0)
            throw new IllegalArgumentException("Precio, cantidad y mínimo deben ser valores válidos no negativos.");
        codigo = codigo.trim(); nombre = nombre.trim(); categoria = categoria.trim();
        descripcion = descripcion == null ? "" : descripcion.trim();
    }
    public Document toDocument() {
        return new Document("_id", codigo).append("nombre", nombre).append("descripcion", descripcion)
            .append("categoria", categoria).append("precio", precio).append("cantidad", cantidad)
            .append("stockMinimo", stockMinimo);
    }
    public static Producto fromDocument(Document d) {
        return new Producto(d.getString("_id"), d.getString("nombre"), d.getString("descripcion"),
            d.getString("categoria"), d.getDouble("precio"), d.getInteger("cantidad"), d.getInteger("stockMinimo", 0));
    }
    public double valorTotal() { return precio * cantidad; }
}
