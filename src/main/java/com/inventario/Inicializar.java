package com.inventario;

import java.util.Arrays;

public class Inicializar {
    public static void main(String[] args) {
        String password = System.getenv("INVENTARIO_DEMO_PASSWORD");
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Configura INVENTARIO_DEMO_PASSWORD antes de inicializar.");
        }
        try (Repositorio repo = new Repositorio()) {
            char[] clave = password.toCharArray();
            try { new Autenticacion(repo).crearUsuarioSiNoExiste("demostracion", clave); }
            finally { Arrays.fill(clave, '\0'); }
            Producto[] ejemplos = {
                new Producto("P001","Laptop","Equipo portátil","Tecnología",650,8,2),
                new Producto("P002","Mouse","Mouse USB","Tecnología",12.5,30,5),
                new Producto("P003","Teclado","Teclado USB","Tecnología",22,12,3),
                new Producto("P004","Cuaderno","Cuaderno rayado","Papelería",2.5,40,10),
                new Producto("P005","Bolígrafo","Tinta azul","Papelería",0.75,60,15),
                new Producto("P006","Monitor","Monitor LED","Tecnología",180,4,2)
            };
            for (Producto p : ejemplos) if (repo.buscar(p.codigo()) == null) repo.agregar(p);
            System.out.println("Datos de demostración listos. Usuario: demostracion");
        }
    }
}
