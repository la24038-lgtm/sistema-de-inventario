package com.inventario;

import com.mongodb.client.MongoCollection;
import org.bson.Document;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.*;
import java.util.*;
import static com.mongodb.client.model.Filters.eq;

public class Autenticacion {
    private static final int ITERACIONES = 210000;
    private final MongoCollection<Document> usuarios;
    public Autenticacion(Repositorio repo) { usuarios = repo.usuarios(); }
    public static String hash(char[] password, byte[] sal) {
        PBEKeySpec spec = new PBEKeySpec(password, sal, ITERACIONES, 256);
        try { return Base64.getEncoder().encodeToString(SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded()); }
        catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
        finally { spec.clearPassword(); }
    }
    public void crearUsuarioSiNoExiste(String nombre, char[] clave) {
        if (usuarios.find(eq("_id", nombre)).first() != null) return;
        byte[] sal = new byte[16]; new SecureRandom().nextBytes(sal);
        usuarios.insertOne(new Document("_id", nombre).append("sal", Base64.getEncoder().encodeToString(sal))
            .append("hash", hash(clave, sal)).append("iteraciones", ITERACIONES));
    }
    public boolean validar(String nombre, char[] clave) {
        Document usuario = usuarios.find(eq("_id", nombre)).first();
        if (usuario == null) return false;
        byte[] sal = Base64.getDecoder().decode(usuario.getString("sal"));
        String almacenado = usuario.getString("hash");
        int iter = usuario.getInteger("iteraciones", ITERACIONES);
        PBEKeySpec spec = new PBEKeySpec(clave, sal, iter, 256);
        try {
            byte[] candidato = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
            return MessageDigest.isEqual(candidato, Base64.getDecoder().decode(almacenado));
        } catch (GeneralSecurityException e) { throw new IllegalStateException(e); }
        finally { spec.clearPassword(); }
    }
}
