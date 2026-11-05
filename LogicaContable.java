import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

public class LogicaContable {
    // Listas paralelas para el Catálogo de Cuentas
    public static ArrayList<String> codigosCuentas = new ArrayList<>();
    public static ArrayList<String> nombresCuentas = new ArrayList<>();
    public static ArrayList<String> tiposCuentas = new ArrayList<>(); 
    public static ArrayList<Double> saldosCuentas = new ArrayList<>();

    // Listas para el Libro Diario / Transacciones
    public static ArrayList<Integer> numAsientos = new ArrayList<>();
    public static ArrayList<String> fechasMovimientos = new ArrayList<>();
    public static ArrayList<String> cuentasDebe = new ArrayList<>();
    public static ArrayList<String> cuentasHaber = new ArrayList<>();
    public static ArrayList<Double> importesMovimientos = new ArrayList<>();
    public static ArrayList<String> conceptosMovimientos = new ArrayList<>();

    private static final String ARCHIVO_CSV = "catalogo.csv";
    private static final String ARCHIVO_DIARIO = "libro_diario.txt";
    private static final String ARCHIVO_MAYOR = "libro_mayor.txt";
    private static final String ARCHIVO_TRANSACCIONES = "transacciones.csv";

    // --- CARGAR CATÁLOGO DESDE catalogo.csv ---
    public static void cargarCatalogoCSV() {
        File archivo = new File(ARCHIVO_CSV);
        if (!archivo.exists()) {
            return;
        }
        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            codigosCuentas.clear();
            nombresCuentas.clear();
            tiposCuentas.clear();
            saldosCuentas.clear();
            String linea;

            while ((linea = br.readLine()) != null) {
                linea = linea.trim();
                if (linea.isEmpty()) continue;

                String separador = linea.contains(";") ? ";" : ",";
                String[] partes = linea.split(separador);

                if (partes.length >= 3) {
                    try {
                        String rawSaldo = partes[partes.length - 1].trim()
                                .replace("C$", "").replace("$", "").replace(" ", "");
                        double saldo = Double.parseDouble(rawSaldo);
                        
                        String codigo = partes[0].trim().replace("\"", "");
                        String tipo = partes[partes.length - 2].trim().replace("\"", "");
                        
                        StringBuilder nombreBuilder = new StringBuilder();
                        for (int j = 1; j <= partes.length - 3; j++) {
                            nombreBuilder.append(partes[j].trim());
                            if (j < partes.length - 3) nombreBuilder.append(", ");
                        }
                        String nombre = nombreBuilder.toString().replace("\"", "").trim();
                        
                        if (!codigosCuentas.contains(codigo)) {
                            codigosCuentas.add(codigo);
                            nombresCuentas.add(nombre);
                            tiposCuentas.add(tipo);
                            saldosCuentas.add(saldo);
                        }
                    } catch (NumberFormatException e) {
                        continue; 
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al leer el archivo catalogo.csv: " + e.getMessage());
        }
    }

    public static void guardarCatalogoCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_CSV))) {
            pw.println("Codigo,Nombre,Tipo,Saldo");
            for (int i = 0; i < nombresCuentas.size(); i++) {
                pw.println(codigosCuentas.get(i) + ",\"" + nombresCuentas.get(i) + "\"," + 
                           tiposCuentas.get(i) + "," + saldosCuentas.get(i));
            }
        } catch (IOException e) {
            System.out.println("Error al escribir en el archivo catalogo.csv: " + e.getMessage());
        }
    }

    // --- CARGAR Y GUARDAR TRANSACCIONES ---
    public static void guardarTransaccionesCSV() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(ARCHIVO_TRANSACCIONES))) {
            pw.println("Asiento,Fecha,Debe,Haber,Importe,Concepto");
            for (int i = 0; i < numAsientos.size(); i++) {
                pw.println(numAsientos.get(i) + "," + 
                           fechasMovimientos.get(i) + "," + 
                           "\"" + cuentasDebe.get(i) + "\"," + 
                           "\"" + cuentasHaber.get(i) + "\"," + 
                           importesMovimientos.get(i) + "," + 
                           "\"" + conceptosMovimientos.get(i) + "\"");
            }
        } catch (IOException e) {
            System.out.println("Error al guardar transacciones: " + e.getMessage());
        }
    }

    public static void cargarTransaccionesCSV() {
        File archivo = new File(ARCHIVO_TRANSACCIONES);
        if (!archivo.exists()) return;

        try (BufferedReader br = new BufferedReader(new FileReader(archivo))) {
            numAsientos.clear();
            fechasMovimientos.clear();
            cuentasDebe.clear();
            cuentasHaber.clear();
            importesMovimientos.clear();
            conceptosMovimientos.clear();

            String linea;
            boolean primeraLinea = true;
            while ((linea = br.readLine()) != null) {
                if (primeraLinea) { primeraLinea = false; continue; }
                String[] partes = linea.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                if (partes.length >= 6) {
                    try {
                        int asiento = Integer.parseInt(partes[0].trim());
                        String fecha = partes[1].trim();
                        String cDebe = partes[2].trim().replace("\"", "");
                        String cHaber = partes[3].trim().replace("\"", "");
                        double importe = Double.parseDouble(partes[4].trim());
                        String concepto = partes[5].trim().replace("\"", "");

                        numAsientos.add(asiento);
                        fechasMovimientos.add(fecha);
                        cuentasDebe.add(cDebe);
                        cuentasHaber.add(cHaber);
                        importesMovimientos.add(importe);
                        conceptosMovimientos.add(concepto);
                    } catch (Exception e) {
                        continue;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Error al cargar transacciones: " + e.getMessage());
        }
    }

    // --- ACTUALIZAR SALDO NETO SEGÚN NATURALEZA ---
    public static void actualizarSaldoCuenta(String nombreCuenta, double importe, boolean esDebe) {
        int idx = nombresCuentas.indexOf(nombreCuenta);
        if (idx == -1) return;
        String tipo = tiposCuentas.get(idx).toUpperCase();
        boolean esDeudora = tipo.contains("ACTIVO") || tipo.contains("COMPRAS") || 
                            tipo.contains("COSTO") || tipo.contains("GASTO") || 
                            tipo.contains("DEDUCCIONES");
        double saldoActual = saldosCuentas.get(idx);
        if (esDeudora) {
            if (esDebe) saldoActual += importe;
            else saldoActual -= importe;
        } else {
            if (esDebe) saldoActual -= importe;
            else saldoActual += importe;
        }
        saldosCuentas.set(idx, saldoActual);
    }

    public static void mostrarCuentas() {
        for (int i = 0; i < nombresCuentas.size(); i++) {
            System.out.println(codigosCuentas.get(i) + " | " + nombresCuentas.get(i) +
                    " | " + tiposCuentas.get(i) + " | C$ " + saldosCuentas.get(i));
        }
    }

    // --- CUENTAS T: solo cuentas afectadas por algun asiento ---
    public static void consultarCuentasT() {
        boolean hayAfectadas = false;
        System.out.println("\n===========================================");
        System.out.println("        CUENTAS T (CUENTAS AFECTADAS)       ");
        System.out.println("===========================================");

        for (int i = 0; i < nombresCuentas.size(); i++) {
            String nombre = nombresCuentas.get(i);

            // Movimientos de esta cuenta
            ArrayList<String> lineasDebe = new ArrayList<>();
            ArrayList<String> lineasHaber = new ArrayList<>();
            double totalDebe = 0;
            double totalHaber = 0;

            for (int j = 0; j < numAsientos.size(); j++) {
                if (cuentasDebe.get(j).equalsIgnoreCase(nombre)) {
                    double imp = importesMovimientos.get(j);
                    totalDebe += imp;
                    lineasDebe.add(String.format(java.util.Locale.US, "%,.2f (As.%d)", imp, numAsientos.get(j)));
                }
                if (cuentasHaber.get(j).equalsIgnoreCase(nombre)) {
                    double imp = importesMovimientos.get(j);
                    totalHaber += imp;
                    lineasHaber.add(String.format(java.util.Locale.US, "%,.2f (As.%d)", imp, numAsientos.get(j)));
                }
            }

            // Si no tiene movimientos, no se muestra
            if (lineasDebe.isEmpty() && lineasHaber.isEmpty()) continue;
            hayAfectadas = true;

            String tipo = tiposCuentas.get(i).toUpperCase();
            boolean esDeudora = tipo.contains("ACTIVO") || tipo.contains("COMPRAS") ||
                                tipo.contains("COSTO") || tipo.contains("GASTO") ||
                                tipo.contains("DEDUCCIONES");

            System.out.println("\nCuenta: " + nombre + " (" + codigosCuentas.get(i) + ") - " + tiposCuentas.get(i));
            System.out.println(String.format("%-24s|%-24s", "          DEBE", "          HABER"));
            System.out.println("------------------------+------------------------");

            int filas = Math.max(lineasDebe.size(), lineasHaber.size());
            for (int k = 0; k < filas; k++) {
                String izq = k < lineasDebe.size() ? lineasDebe.get(k) : "";
                String der = k < lineasHaber.size() ? lineasHaber.get(k) : "";
                System.out.println(String.format(" %-22s| %-22s", izq, der));
            }

            System.out.println("------------------------+------------------------");
            System.out.println(String.format(java.util.Locale.US, " %-22s| %-22s",
                    "Total: " + String.format(java.util.Locale.US, "%,.2f", totalDebe),
                    "Total: " + String.format(java.util.Locale.US, "%,.2f", totalHaber)));

            double neto = esDeudora ? (totalDebe - totalHaber) : (totalHaber - totalDebe);
            String naturaleza;
            if (Math.abs(neto) < 0.005) {
                naturaleza = "Saldado";
            } else if (neto > 0) {
                naturaleza = esDeudora ? "Deudor" : "Acreedor";
            } else {
                naturaleza = esDeudora ? "Acreedor" : "Deudor";
            }
            System.out.println(String.format(java.util.Locale.US, "Saldo del periodo: C$ %,.2f (%s)",
                    Math.abs(neto), naturaleza));
        }

        if (!hayAfectadas) {
            System.out.println("\nNo hay cuentas afectadas todavia. Registre un asiento primero (opcion 2).");
        }
    }

    public static void generarLibroDiario() {
        for (int i = 0; i < numAsientos.size(); i++) {
            System.out.println("Asiento #" + numAsientos.get(i) + " | Fecha: " + fechasMovimientos.get(i) +
                    " | " + conceptosMovimientos.get(i));
            System.out.println("Debe: " + cuentasDebe.get(i) + " | Haber: " + cuentasHaber.get(i) +
                    " | Importe: C$ " + importesMovimientos.get(i));
        }
    }

    public static void generarLibroMayor() {
        consultarCuentasT();
    }

    public static void generarBalanzaComprobacion() {
        double totalDebe = 0;
        double totalHaber = 0;
        for (int i = 0; i < numAsientos.size(); i++) {
            if (!cuentasDebe.get(i).equals("---")) totalDebe += importesMovimientos.get(i);
            if (!cuentasHaber.get(i).equals("---")) totalHaber += importesMovimientos.get(i);
        }
        System.out.println("Total Debe: C$ " + totalDebe);
        System.out.println("Total Haber: C$ " + totalHaber);
    }

    public static void generarEstadoResultados() {
        double ingresos = 0;
        double gastos = 0;
        for (int i = 0; i < nombresCuentas.size(); i++) {
            String tipo = tiposCuentas.get(i).toUpperCase();
            if (tipo.contains("INGRESO")) ingresos += saldosCuentas.get(i);
            if (tipo.contains("GASTO") || tipo.contains("COSTO") || tipo.contains("COMPRAS")) {
                gastos += saldosCuentas.get(i);
            }
        }
        System.out.println("Ingresos: C$ " + ingresos);
        System.out.println("Gastos: C$ " + gastos);
        System.out.println("Utilidad neta: C$ " + (ingresos - gastos));
    }

    public static void generarBalanceGeneral() {
        double activo = 0;
        double pasivo = 0;
        double capital = 0;
        for (int i = 0; i < nombresCuentas.size(); i++) {
            String tipo = tiposCuentas.get(i).toUpperCase();
            if (tipo.contains("ACTIVO")) activo += saldosCuentas.get(i);
            else if (tipo.contains("PASIVO")) pasivo += saldosCuentas.get(i);
            else if (tipo.contains("CAPITAL")) capital += saldosCuentas.get(i);
        }
        System.out.println("Activo: C$ " + activo);
        System.out.println("Pasivo: C$ " + pasivo);
        System.out.println("Capital: C$ " + capital);
    }

    // --- 1. ADMINISTRAR CUENTAS ---
    public static void administrarCuentas(Scanner scanner) {
        cargarCatalogoCSV();
        int op = 0;
        do {
            System.out.println("\n===========================================");
            System.out.println("      ADMINISTRAR CATÁLOGO DE CUENTAS      ");
            System.out.println("===========================================");
            System.out.println("1. Agregar nueva cuenta");
            System.out.println("2. Buscar cuenta");
            System.out.println("3. Modificar cuenta");
            System.out.println("4. Eliminar cuenta o monto (Submenú)");
            System.out.println("5. Mostrar todas las cuentas");
            System.out.println("6. Regresar al Menú Principal");
            System.out.print("Seleccione una opción: ");

            if (scanner.hasNextInt()) {
                op = scanner.nextInt();
                scanner.nextLine();
                switch (op) {
                    case 1: agregarCuenta(scanner); break;
                    case 2: buscarCuenta(scanner); break;
                    case 3: modificarCuenta(scanner); break;
                    case 4: menuEliminacionAvanzada(scanner); break;
                    case 5: mostrarCuentas(); break;
                    case 6: break;
                    default: System.out.println("Opción inválida.");
                }
            } else {
                System.out.println("Error: Ingrese un número válido.");
                scanner.next();
            }
        } while (op != 6);
    }

    public static void agregarCuenta(Scanner scanner) {
        System.out.println("\n--- CLASIFICACIÓN PRINCIPAL ---");
        System.out.println("1. Activo (1XX)");
        System.out.println("2. Pasivo (2XX)");
        System.out.println("3. Capital (3XX)");
        System.out.println("4. Ingresos (4XX)");
        System.out.println("5. Gastos y Costos (5XX)");
        System.out.print("Seleccione el grupo principal: ");
        int grupo = scanner.nextInt();
        scanner.nextLine();

        String subclasificacion = "";
        int minRango = 0, maxRango = 0;

        switch (grupo) {
            case 1:
                System.out.println("1. Activo Circulante");
                System.out.println("2. Activo No Circulante");
                int subA = scanner.nextInt(); scanner.nextLine();
                if (subA == 1) { subclasificacion = "ACTIVO CIRCULANTE"; minRango = 101; maxRango = 149; } 
                else { subclasificacion = "ACTIVO NO CIRCULANTE"; minRango = 150; maxRango = 199; }
                break;
            case 2:
                System.out.println("1. Pasivo a Corto Plazo");
                System.out.println("2. Pasivo a Largo Plazo");
                int subP = scanner.nextInt(); scanner.nextLine();
                if (subP == 1) { subclasificacion = "PASIVO A CORTO PLAZO"; minRango = 201; maxRango = 250; } 
                else { subclasificacion = "PASIVO A LARGO PLAZO"; minRango = 251; maxRango = 299; }
                break;
            case 3:
                System.out.println("1. Capital Contribuido");
                System.out.println("2. Capital Ganado");
                int subC = scanner.nextInt(); scanner.nextLine();
                if (subC == 1) { subclasificacion = "CAPITAL CONTRIBUIDO"; minRango = 301; maxRango = 350; } 
                else { subclasificacion = "CAPITAL GANADO"; minRango = 351; maxRango = 399; }
                break;
            case 4:
                subclasificacion = "INGRESOS"; minRango = 401; maxRango = 499;
                break;
            case 5:
                System.out.println("1. Compras y Costo de Ventas");
                System.out.println("2. Gastos de Venta");
                System.out.println("3. Gastos de Administración");
                System.out.println("4. Gastos Financieros");
                System.out.println("5. Gastos por Impuestos");
                System.out.println("6. Otros Gastos");
                int subG = scanner.nextInt(); scanner.nextLine();
                switch(subG) {
                    case 1: subclasificacion = "COMPRAS Y COSTO DE VENTAS"; minRango = 501; maxRango = 506; break;
                    case 2: subclasificacion = "GASTOS DE VENTA"; minRango = 507; maxRango = 521; break;
                    case 3: subclasificacion = "GASTOS DE ADMINISTRACIÓN"; minRango = 522; maxRango = 529; break;
                    case 4: subclasificacion = "GASTOS FINANCIEROS"; minRango = 530; maxRango = 533; break;
                    case 5: subclasificacion = "GASTOS POR IMPUESTOS"; minRango = 534; maxRango = 539; break;
                    case 6: subclasificacion = "OTROS GASTOS"; minRango = 540; maxRango = 599; break;
                    default: minRango = 501; maxRango = 599; subclasificacion = "GASTOS GENERALES";
                }
                break;
            default:
                System.out.println("Grupo no válido."); return;
        }

        String codigoAsignado = "";
        for (int r = minRango; r <= maxRango; r++) {
            String candidato = String.valueOf(r);
            if (!codigosCuentas.contains(candidato)) {
                codigoAsignado = candidato;
                break;
            }
        }

        if (codigoAsignado.isEmpty()) {
            System.out.println("Error: El rango para esta subclasificación está lleno.");
            return;
        }

        System.out.print("Ingrese el nombre de la nueva cuenta: ");
        String nombre = scanner.nextLine().trim();
        if (nombresCuentas.contains(nombre)) {
            System.out.println("Error: Ya existe una cuenta con ese nombre.");
            return;
        }

        codigosCuentas.add(codigoAsignado);
        nombresCuentas.add(nombre);
        tiposCuentas.add(subclasificacion);
        saldosCuentas.add(0.0);

        guardarCatalogoCSV();
        System.out.println("¡Cuenta agregada y guardada en catalogo.csv! Código asignado: " + codigoAsignado);
    }

    public static void buscarCuenta(Scanner scanner) {
        System.out.print("Ingrese el nombre o código de la cuenta a buscar: ");
        String busqueda = scanner.nextLine().trim();
        boolean encontrado = false;
        System.out.println("\n--- RESULTADOS DE BÚSQUEDA ---");
        for (int i = 0; i < nombresCuentas.size(); i++) {
            if (codigosCuentas.get(i).equalsIgnoreCase(busqueda) || nombresCuentas.get(i).toLowerCase().contains(busqueda.toLowerCase())) {
                System.out.println("Código: " + codigosCuentas.get(i) + " | Cuenta: " + nombresCuentas.get(i) + 
                                   " | Tipo: " + tiposCuentas.get(i) + " | Saldo: C$ " + saldosCuentas.get(i));
                encontrado = true;
            }
        }
        if (!encontrado) System.out.println("No se encontró ninguna cuenta coincidente.");
    }

    public static void modificarCuenta(Scanner scanner) {
        mostrarCuentas();
        System.out.print("Ingrese el código o nombre exacto de la cuenta a modificar: ");
        String busqueda = scanner.nextLine().trim();
        int idx = -1;
        for (int i = 0; i < nombresCuentas.size(); i++) {
            if (codigosCuentas.get(i).equalsIgnoreCase(busqueda) || nombresCuentas.get(i).equalsIgnoreCase(busqueda)) {
                idx = i; break;
            }
        }

        if (idx == -1) {
            System.out.println("Error: Cuenta no encontrada.");
            return;
        }

        System.out.println("Cuenta seleccionada: " + nombresCuentas.get(idx) + " (Saldo actual: C$ " + saldosCuentas.get(idx) + ")");
        if (saldosCuentas.get(idx) != 0.0) {
            System.out.println("ADVERTENCIA: Esta cuenta tiene un monto/saldo activo.");
            System.out.print("¿Desea continuar con la modificación? (s/n): ");
            if (!scanner.nextLine().trim().equalsIgnoreCase("s")) return;
        }

        System.out.print("Nuevo nombre de la cuenta: ");
        String nuevoNombre = scanner.nextLine().trim();
        if (!nombresCuentas.get(idx).equalsIgnoreCase(nuevoNombre) && nombresCuentas.contains(nuevoNombre)) {
            System.out.println("Error: Ya existe otra cuenta con ese nombre.");
            return;
        }
        nombresCuentas.set(idx, nuevoNombre);
        guardarCatalogoCSV();
        System.out.println("¡Cuenta modificada y actualizada en catalogo.csv con éxito!");
    }

    public static void menuEliminacionAvanzada(Scanner scanner) {
        System.out.print("Ingrese el nombre o código de la cuenta a gestionar: ");
        String busqueda = scanner.nextLine().trim();
        int idx = -1;
        for (int i = 0; i < nombresCuentas.size(); i++) {
            if (codigosCuentas.get(i).equalsIgnoreCase(busqueda) || nombresCuentas.get(i).equalsIgnoreCase(busqueda)) {
                idx = i; break;
            }
        }

        if (idx == -1) {
            System.out.println("Error: Cuenta no encontrada.");
            return;
        }

        String nombreCuenta = nombresCuentas.get(idx);
        double saldoActual = saldosCuentas.get(idx);

        System.out.println("\n--- SUBMENÚ DE ELIMINACIÓN ---");
        System.out.println("Cuenta: " + nombreCuenta + " | Saldo acumulado: C$ " + saldoActual);
        System.out.println("1. Eliminar la cuenta completa en sí");
        System.out.println("2. Eliminar solo un monto o transacción específica de esta cuenta");
        System.out.print("Seleccione una opción: ");
        int opcionSub = scanner.nextInt();
        scanner.nextLine();

        if (opcionSub == 1) {
            if (saldoActual != 0.0) {
                System.out.println("¡ALERTA! La cuenta tiene un monto activo de C$ " + saldoActual + ".");
                System.out.print("¿Está SEGURO de eliminar la cuenta completa? (s/n): ");
                String seguro = scanner.nextLine().trim();
                if (!seguro.equalsIgnoreCase("s")) return;
            } else {
                System.out.print("¿Está seguro de eliminar la cuenta? (s/n): ");
                if (!scanner.nextLine().trim().equalsIgnoreCase("s")) return;
            }

            codigosCuentas.remove(idx);
            nombresCuentas.remove(idx);
            tiposCuentas.remove(idx);
            saldosCuentas.remove(idx);
            guardarCatalogoCSV();
            System.out.println("¡Cuenta eliminada con éxito y catalogo.csv actualizado!");

         } else if (opcionSub == 2) {
            ArrayList<Integer> indicesTransacciones = new ArrayList<>();
            System.out.println("\n--- TRANSACCIONES / MONTOS ASOCIADOS A " + nombreCuenta + " ---");
            for (int j = 0; j < numAsientos.size(); j++) {
                if (cuentasDebe.get(j).equalsIgnoreCase(nombreCuenta) || cuentasHaber.get(j).equalsIgnoreCase(nombreCuenta)) {
                    indicesTransacciones.add(j);
                    System.out.println("[" + indicesTransacciones.size() + "] Asiento #" + numAsientos.get(j) + 
                                       " | Fecha: " + fechasMovimientos.get(j) + 
                                       " | Debe: " + cuentasDebe.get(j) + 
                                       " | Haber: " + cuentasHaber.get(j) + 
                                       " | Importe: C$ " + importesMovimientos.get(j));
                }
            }

            if (indicesTransacciones.isEmpty()) {
                System.out.println("No hay montos o transacciones vinculadas a esta cuenta.");
                return;
            }

            System.out.print("Seleccione el número de la transacción que desea eliminar: ");
            int seleccionTrans = scanner.nextInt();
            scanner.nextLine();

            if (seleccionTrans < 1 || seleccionTrans > indicesTransacciones.size()) {
                System.out.println("Selección inválida.");
                return;
            }

            int indexReal = indicesTransacciones.get(seleccionTrans - 1);
            double montoAEliminar = importesMovimientos.get(indexReal);
            String cDebe = cuentasDebe.get(indexReal);
            String cHaber = cuentasHaber.get(indexReal);

            System.out.println("Va a eliminar la transacción por C$ " + montoAEliminar + " (" + cDebe + " a " + cHaber + ").");
            System.out.print("¿Está SEGURO de eliminar este monto? Esto recalculará las cuentas (s/n): ");
            if (scanner.nextLine().trim().equalsIgnoreCase("s")) {
                numAsientos.remove(indexReal);
                fechasMovimientos.remove(indexReal);
                cuentasDebe.remove(indexReal);
                cuentasHaber.remove(indexReal);
                importesMovimientos.remove(indexReal);
                conceptosMovimientos.remove(indexReal);
                guardarTransaccionesCSV();
                System.out.println("Movimiento eliminado.");
            }
        }
    }
}