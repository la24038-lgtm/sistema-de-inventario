package com.inventario;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.*;

public class Main {
    private final Repositorio repo;
    private final Autenticacion auth;
    private JFrame ventana;
    private JTable tabla;
    private DefaultTableModel modelo;
    private JTextField filtro, categoriaFiltro;
    private java.util.List<Producto> visibles = java.util.List.of();
    private String usuarioActivo;
    private Main(Repositorio repo) { this.repo = repo; auth = new Autenticacion(repo); }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try { Repositorio repo = new Repositorio(); Main app = new Main(repo); Runtime.getRuntime().addShutdownHook(new Thread(repo::close)); app.login(); }
            catch (Exception e) { JOptionPane.showMessageDialog(null, "No se pudo conectar con MongoDB.\n" + e.getMessage(), "Error de conexión", JOptionPane.ERROR_MESSAGE); }
        });
    }
    private void login() {
        if (ventana != null) ventana.dispose();
        JFrame f = new JFrame("Sistema de Inventario - Iniciar sesión"); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel(new GridLayout(3,2,10,10)); panel.setBorder(BorderFactory.createEmptyBorder(25,25,25,25));
        JTextField usuario = new JTextField(); JPasswordField clave = new JPasswordField();
        panel.add(new JLabel("Usuario:")); panel.add(usuario); panel.add(new JLabel("Contraseña:")); panel.add(clave);
        JButton ingresar = new JButton("Iniciar sesión"); panel.add(new JLabel()); panel.add(ingresar);
        ingresar.addActionListener(e -> {
            char[] pass = clave.getPassword();
            try { if (auth.validar(usuario.getText().trim(), pass)) { usuarioActivo = usuario.getText().trim(); f.dispose(); principal(); }
                  else JOptionPane.showMessageDialog(f,"Usuario o contraseña incorrectos."); }
            catch (Exception ex) { error(f,ex); }
            finally { Arrays.fill(pass, '\0'); clave.setText(""); }
        });
        f.add(panel); f.pack(); f.setMinimumSize(new Dimension(390,190)); f.setLocationRelativeTo(null); f.setVisible(true);
    }
    private JButton boton(String texto, Runnable accion) { JButton b = new JButton(texto); b.addActionListener(e -> { try { accion.run(); } catch (Exception ex) { error(ventana,ex); } }); return b; }
    private void principal() {
        ventana = new JFrame("Sistema de Inventario - " + usuarioActivo); ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventana.setSize(1050,650); ventana.setLocationRelativeTo(null);
        JPanel arriba = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filtro = new JTextField(18); categoriaFiltro = new JTextField(12);
        arriba.add(new JLabel("Código o nombre:")); arriba.add(filtro); arriba.add(new JLabel("Categoría:")); arriba.add(categoriaFiltro);
        arriba.add(boton("Buscar", this::refrescar)); arriba.add(boton("Limpiar", () -> { filtro.setText(""); categoriaFiltro.setText(""); refrescar(); }));
        modelo = new DefaultTableModel(new Object[]{"Código","Nombre","Descripción","Categoría","Precio","Cantidad","Mínimo","Valor"},0) {
            @Override public boolean isCellEditable(int r,int c) { return false; }
        };
        tabla = new JTable(modelo); tabla.setAutoCreateRowSorter(true); tabla.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JPanel abajo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        abajo.add(boton("Nuevo producto", () -> editar(false)));
        abajo.add(boton("Editar", () -> editar(true)));
        abajo.add(boton("Eliminar", this::eliminar));
        abajo.add(boton("Entrada", () -> mover(true)));
        abajo.add(boton("Salida", () -> mover(false)));
        abajo.add(boton("Reportes", this::reportes));
        abajo.add(boton("Cerrar sesión", () -> { ventana.dispose(); login(); }));
        ventana.add(arriba,BorderLayout.NORTH); ventana.add(new JScrollPane(tabla),BorderLayout.CENTER); ventana.add(abajo,BorderLayout.SOUTH);
        refrescar(); ventana.setVisible(true);
    }
    private void refrescar() {
        visibles = repo.listar(filtro.getText(), categoriaFiltro.getText()); modelo.setRowCount(0);
        for (Producto p:visibles) modelo.addRow(new Object[]{p.codigo(),p.nombre(),p.descripcion(),p.categoria(),p.precio(),p.cantidad(),p.stockMinimo(),p.valorTotal()});
    }
    private Producto seleccionado() {
        int row = tabla.getSelectedRow(); if (row < 0) throw new IllegalArgumentException("Selecciona un producto de la tabla.");
        return visibles.get(tabla.convertRowIndexToModel(row));
    }
    private void editar(boolean modificar) {
        Producto p = modificar ? seleccionado() : null;
        JTextField codigo = new JTextField(p == null ? "" : p.codigo()); codigo.setEditable(!modificar);
        JTextField nombre = new JTextField(p == null ? "" : p.nombre());
        JTextField descripcion = new JTextField(p == null ? "" : p.descripcion());
        JTextField categoria = new JTextField(p == null ? "" : p.categoria());
        JTextField precio = new JTextField(p == null ? "0" : String.valueOf(p.precio()));
        JTextField cantidad = new JTextField(p == null ? "0" : String.valueOf(p.cantidad())); cantidad.setEditable(!modificar);
        JTextField minimo = new JTextField(p == null ? "0" : String.valueOf(p.stockMinimo()));
        JPanel form = new JPanel(new GridLayout(7,2,8,8));
        String[] nombres = {"Código","Nombre","Descripción","Categoría","Precio","Cantidad inicial","Stock mínimo"};
        JTextField[] campos = {codigo,nombre,descripcion,categoria,precio,cantidad,minimo};
        for(int i=0;i<nombres.length;i++) { form.add(new JLabel(nombres[i])); form.add(campos[i]); }
        if (JOptionPane.showConfirmDialog(ventana,form,modificar?"Editar producto":"Nuevo producto",JOptionPane.OK_CANCEL_OPTION)==JOptionPane.OK_OPTION) {
            Producto nuevo = new Producto(codigo.getText(),nombre.getText(),descripcion.getText(),categoria.getText(),
                Double.parseDouble(precio.getText()), modificar?p.cantidad():Integer.parseInt(cantidad.getText()),Integer.parseInt(minimo.getText()));
            if (modificar) repo.actualizar(nuevo); else repo.agregar(nuevo);
            refrescar();
        }
    }
    private void eliminar() {
        Producto p = seleccionado();
        if (JOptionPane.showConfirmDialog(ventana,"¿Eliminar " + p.nombre() + "?","Confirmar",JOptionPane.YES_NO_OPTION)==JOptionPane.YES_OPTION) {
            repo.eliminar(p.codigo()); refrescar();
        }
    }
    private void mover(boolean entrada) {
        Producto p = seleccionado(); String valor = JOptionPane.showInputDialog(ventana,"Unidades para " + (entrada?"entrada":"salida") + " de " + p.nombre() + ":");
        if (valor == null) return;
        repo.mover(p.codigo(),Integer.parseInt(valor.trim()),entrada); refrescar();
    }
    private void reportes() {
        java.util.List<Producto> todos = repo.listar("", "");
        long agotados = todos.stream().filter(p -> p.cantidad()==0).count();
        long bajos = todos.stream().filter(p -> p.cantidad()<=p.stockMinimo()).count();
        double valor = todos.stream().mapToDouble(Producto::valorTotal).sum();
        StringBuilder s = new StringBuilder("INVENTARIO GENERAL\nProductos: "+todos.size()+"\nAgotados: "+agotados+"\nExistencias bajas: "+bajos+"\nValor total: $"+String.format("%.2f",valor)+"\n\nAGOTADOS O BAJO MÍNIMO\n");
        todos.stream().filter(p -> p.cantidad()<=p.stockMinimo()).forEach(p -> s.append(p.codigo()).append(" - ").append(p.nombre()).append(" (stock: ").append(p.cantidad()).append(", mínimo: ").append(p.stockMinimo()).append(")\n"));
        JTextArea area = new JTextArea(s.toString(),20,55); area.setEditable(false);
        JOptionPane.showMessageDialog(ventana,new JScrollPane(area),"Reportes",JOptionPane.INFORMATION_MESSAGE);
    }
    private static void error(Component padre,Exception e) { JOptionPane.showMessageDialog(padre,e.getMessage(),"Error",JOptionPane.ERROR_MESSAGE); }
}
