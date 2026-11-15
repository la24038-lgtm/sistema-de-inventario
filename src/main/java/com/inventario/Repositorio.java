package com.inventario;

import com.mongodb.MongoWriteException;
import com.mongodb.client.*;
import com.mongodb.client.model.*;
import org.bson.Document;
import org.bson.conversions.Bson;
import java.util.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

public class Repositorio implements AutoCloseable {
    private final MongoClient client;
    private final MongoDatabase db;
    private final MongoCollection<Document> productos;
    public Repositorio() {
        String uri = System.getenv().getOrDefault("MONGO_URI", "mongodb://localhost:27017");
        String nombre = System.getenv().getOrDefault("MONGO_DB", "inventario_g5_gp2");
        client = MongoClients.create(uri);
        db = client.getDatabase(nombre);
        db.runCommand(new Document("ping", 1));
        productos = db.getCollection("productos");
    }
    public MongoCollection<Document> usuarios() { return db.getCollection("usuarios"); }
    public void agregar(Producto p) {
        try { productos.insertOne(p.toDocument()); }
        catch (MongoWriteException ex) { if (ex.getError().getCode() == 11000) throw new IllegalArgumentException("Ya existe un producto con ese código."); throw ex; }
    }
    public void actualizar(Producto p) {
        var result = productos.replaceOne(eq("_id", p.codigo()), p.toDocument());
        if (result.getMatchedCount() == 0) throw new IllegalArgumentException("El producto no existe.");
    }
    public void eliminar(String codigo) {
        if (productos.deleteOne(eq("_id", codigo)).getDeletedCount() == 0) throw new IllegalArgumentException("El producto no existe.");
    }
    public Producto buscar(String codigo) { Document d = productos.find(eq("_id", codigo)).first(); return d == null ? null : Producto.fromDocument(d); }
    public List<Producto> listar(String texto, String categoria) {
        List<Bson> condiciones = new ArrayList<>();
        if (texto != null && !texto.isBlank()) {
            String seguro = java.util.regex.Pattern.quote(texto.trim());
            condiciones.add(or(regex("_id", seguro, "i"), regex("nombre", seguro, "i")));
        }
        if (categoria != null && !categoria.isBlank()) condiciones.add(eq("categoria", categoria.trim()));
        Bson filtro = condiciones.isEmpty() ? new Document() : and(condiciones);
        List<Producto> lista = new ArrayList<>();
        for (Document d : productos.find(filtro).sort(Sorts.ascending("nombre"))) lista.add(Producto.fromDocument(d));
        return lista;
    }
    public void mover(String codigo, int unidades, boolean entrada) {
        if (unidades <= 0) throw new IllegalArgumentException("La cantidad debe ser mayor que cero.");
        Bson filtro = entrada ? eq("_id", codigo) : and(eq("_id", codigo), gte("cantidad", unidades));
        Document anterior = productos.findOneAndUpdate(filtro, inc("cantidad", entrada ? unidades : -unidades));
        if (anterior == null) throw new IllegalArgumentException("Producto inexistente o existencias insuficientes.");
    }
    @Override public void close() { client.close(); }
}
