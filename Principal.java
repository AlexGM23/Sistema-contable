import java.util.Scanner;
import java.util.ArrayList;

public class Principal {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int opcion = 0;

        // Cargar el catálogo y las transacciones previas al arrancar
        LogicaContable.cargarCatalogoCSV();
        LogicaContable.cargarTransaccionesCSV(); // Punto 5: Recuperar transacciones persistentes

        do {
            try {
                System.out.println("\n===========================================");
                System.out.println("       SISTEMA CONTABLE EMPRESARIAL        ");
                System.out.println("===========================================");
                System.out.println("1. Administrar cuentas");
                System.out.println("2. Registrar operación contable (Modo Amigable y Compuesto)");
                System.out.println("3. Consultar cuentas T");
                System.out.println("4. Generar libro diario");
                System.out.println("5. Generar libro mayor (Formato Oficial)");
                System.out.println("6. Generar balanza de comprobación");
                System.out.println("7. Generar estado de resultados");
                System.out.println("8. Balance general");
                System.out.println("9. Consultar movimientos (Por fecha ordenadas)");
                System.out.println("10. Respaldar información");
                System.out.println("11. Salir");
                System.out.print("\nSeleccione una opción: ");

                if (scanner.hasNextInt()) {
                    opcion = scanner.nextInt();
                    scanner.nextLine(); // Limpiar buffer

                    switch (opcion) {
                        case 1:
                            LogicaContable.administrarCuentas(scanner);
                            break;
                        case 2:
                            registrarAsientoAmigableCompuesto(scanner);
                            break;
                        case 3:
                            LogicaContable.consultarCuentasT();
                            break;
                        case 4:
                            LogicaContable.generarLibroDiario();
                            break;
                        case 5:
                            LogicaContable.generarLibroMayor();
                            break;
                        case 6:
                            LogicaContable.generarBalanzaComprobacion();
                            break;
                        case 7:
                            LogicaContable.generarEstadoResultados();
                            break;
                        case 8:
                            LogicaContable.generarBalanceGeneral();
                            break;
                        case 9:
                            consultarMovimientosMenu(scanner);
                            break;
                        case 10:
                            LogicaContable.guardarCatalogoCSV();
                            LogicaContable.guardarTransaccionesCSV();
                            System.out.println("¡Información respaldada exitosamente!");
                            break;
                        case 11:
                            LogicaContable.guardarTransaccionesCSV(); // Guardado automático al salir
                            System.out.println("¡Saliendo del sistema contable. Hasta pronto!");
                            break;
                        default:
                            System.out.println("Error: Opción inválida. Intente de nuevo.");
                    }
                } else {
                    System.out.println("Error: Debe ingresar un número entero válido.");
                    scanner.next();
                }
            } catch (Exception e) {
                System.out.println("Ocurrió un error inesperado durante la ejecución: " + e.getMessage());
                scanner.nextLine();
            }
        } while (opcion != 11);

        scanner.close();
    }

    // --- REGISTRO AMIGABLE Y COMPUESTO CON BÚSQUEDA INTERACTIVA (Punto 3) ---
    public static void registrarAsientoAmigableCompuesto(Scanner scanner) {
        System.out.println("\n==================================================");
        System.out.println("   REGISTRO CONTABLE INTELIGENTE (MÚLTIPLES CUENTAS)");
        System.out.println("==================================================");
        
        System.out.print("Número de asiento: ");
        int nAsiento = scanner.nextInt();
        scanner.nextLine();

        System.out.print("Fecha (Formato DD/MM/AAAA): ");
        String fecha = scanner.nextLine().trim();

        System.out.print("Concepto o descripción de lo que sucedió: ");
        String concepto = scanner.nextLine().trim();

        ArrayList<String> cuentasDebeTemp = new ArrayList<>();
        ArrayList<Double> importesDebeTemp = new ArrayList<>();
        ArrayList<String> cuentasHaberTemp = new ArrayList<>();
        ArrayList<Double> importesHaberTemp = new ArrayList<>();

        double sumaDebe = 0;
        double sumaHaber = 0;

        boolean agregandoMovimientos = true;
        int paso = 1;

        while (agregandoMovimientos) {
            System.out.println("\n--- Movimiento #" + paso + " ---");
            System.out.println("(Consejo: Si no recuerdas la cuenta, escribe '?' o 'buscar' para ver el catálogo)");

            int idx = -1;
            String nombreCta = "";
            String tipoCta = "";

            while (idx == -1) {
                System.out.print("Escribe el nombre o código de la cuenta: ");
                String input = scanner.nextLine().trim();

                // Punto 3: Búsqueda interactiva en tiempo de ejecución
                if (input.equalsIgnoreCase("?") || input.equalsIgnoreCase("buscar")) {
                    LogicaContable.mostrarCuentas();
                    System.out.println();
                    continue;
                }

                for (int i = 0; i < LogicaContable.nombresCuentas.size(); i++) {
                    if (LogicaContable.codigosCuentas.get(i).equalsIgnoreCase(input) || 
                        LogicaContable.nombresCuentas.get(i).equalsIgnoreCase(input)) {
                        idx = i;
                        break;
                    }
                }

                if (idx == -1) {
                    System.out.println("Error: La cuenta '" + input + "' no existe. Escribe '?' para buscar en el catálogo.");
                } else {
                    nombreCta = LogicaContable.nombresCuentas.get(idx);
                    tipoCta = LogicaContable.tiposCuentas.get(idx);
                }
            }

            System.out.println("✔ Cuenta encontrada: " + nombreCta + " (" + tipoCta + ")");
            System.out.println("¿Qué le pasó a esta cuenta en esta transacción?");
            System.out.println("1. Aumentó (Entró valor / Creció)");
            System.out.println("2. Disminuyó (Salió valor / Se redujo)");
            System.out.print("Seleccione una opción (1 o 2): ");
            int estado = scanner.nextInt();
            scanner.nextLine();

            while (estado != 1 && estado != 2) {
                System.out.print("Opción inválida. Seleccione 1 (Aumentó) o 2 (Disminuyó): ");
                estado = scanner.nextInt();
                scanner.nextLine();
            }

            System.out.print("Monto de esta cuenta (C$): ");
            double importe = scanner.nextDouble();
            scanner.nextLine();

            if (importe <= 0) {
                System.out.println("El importe debe ser mayor a cero. Se omitirá este movimiento.");
                continue;
            }

            String ladoAsignado = determinarLadoAutomatico(tipoCta, estado);

            if (ladoAsignado.equals("DEBE")) {
                cuentasDebeTemp.add(nombreCta);
                importesDebeTemp.add(importe);
                sumaDebe += importe;
                System.out.println(" -> Clasificado automáticamente al DEBE por C$ " + importe);
            } else {
                cuentasHaberTemp.add(nombreCta);
                importesHaberTemp.add(importe);
                sumaHaber += importe;
                System.out.println(" -> Clasificado automáticamente al HABER por C$ " + importe);
            }

            System.out.print("\n¿Desea agregar otra cuenta a este asiento? (s/n): ");
            String continuar = scanner.nextLine().trim();
            if (!continuar.equalsIgnoreCase("s")) {
                agregandoMovimientos = false;
            }
            paso++;
        }

        // --- VALIDACIÓN DE PARTIDA DOBLE ---
        if (Math.abs(sumaDebe - sumaHaber) > 0.01) {
            System.out.println("\n==================================================");
            System.out.println("  ERROR: El asiento no está cuadrado.");
            System.out.println(" Total en el Debe:  C$ " + sumaDebe);
            System.out.println(" Total en el Haber: C$ " + sumaHaber);
            System.out.println(" La diferencia no permite registrar la operación.");
            System.out.println("==================================================");
            return;
        }

        // --- REGISTRAR EN LAS LISTAS GLOBALES ---
        for (int i = 0; i < cuentasDebeTemp.size(); i++) {
            LogicaContable.numAsientos.add(nAsiento);
            LogicaContable.fechasMovimientos.add(fecha);
            LogicaContable.cuentasDebe.add(cuentasDebeTemp.get(i));
            LogicaContable.cuentasHaber.add("---");
            LogicaContable.importesMovimientos.add(importesDebeTemp.get(i));
            LogicaContable.conceptosMovimientos.add(concepto);

            LogicaContable.actualizarSaldoCuenta(cuentasDebeTemp.get(i), importesDebeTemp.get(i), true);
        }

        for (int i = 0; i < cuentasHaberTemp.size(); i++) {
            LogicaContable.numAsientos.add(nAsiento);
            LogicaContable.fechasMovimientos.add(fecha);
            LogicaContable.cuentasDebe.add("---");
            LogicaContable.cuentasHaber.add(cuentasHaberTemp.get(i));
            LogicaContable.importesMovimientos.add(importesHaberTemp.get(i));
            LogicaContable.conceptosMovimientos.add(concepto);

            LogicaContable.actualizarSaldoCuenta(cuentasHaberTemp.get(i), importesHaberTemp.get(i), false);
        }

        // Guardado persistente inmediato
        LogicaContable.guardarCatalogoCSV();
        LogicaContable.guardarTransaccionesCSV(); // Punto 5

        System.out.println("\n==================================================");
        System.out.println("       ¡ASIENTO REGISTRADO EXITOSAMENTE!          ");
        System.out.println("==================================================");
        System.out.println(" Total Cuadrado: C$ " + sumaDebe);
        System.out.println("==================================================");
    }

    public static String determinarLadoAutomatico(String subclasificacion, int opcionAumentoDisminucion) {
        subclasificacion = subclasificacion.toUpperCase();
        
        boolean esDeudora = subclasificacion.contains("ACTIVO") || 
                            subclasificacion.contains("COMPRAS") || 
                            subclasificacion.contains("COSTO") || 
                            subclasificacion.contains("GASTO") || 
                            subclasificacion.contains("DEDUCCIONES");
        
        if (esDeudora) {
            return (opcionAumentoDisminucion == 1) ? "DEBE" : "HABER";
        } else {
            return (opcionAumentoDisminucion == 1) ? "HABER" : "DEBE";
        }
    }

    public static void consultarMovimientosMenu(Scanner scanner) {
        if (LogicaContable.fechasMovimientos.isEmpty()) {
            System.out.println("No hay movimientos registrados.");
            return;
        }
        System.out.print("Ingrese la fecha a buscar (DD/MM/AAAA): ");
        String busq = scanner.nextLine().trim();
        boolean enc = false;
        System.out.println("\n--- MOVIMIENTOS EN FECHA: " + busq + " ---");
        for (int i = 0; i < LogicaContable.fechasMovimientos.size(); i++) {
            if (LogicaContable.fechasMovimientos.get(i).equalsIgnoreCase(busq)) {
                System.out.println("Asiento #" + LogicaContable.numAsientos.get(i) + " | Debe: " + 
                                   LogicaContable.cuentasDebe.get(i) + " | Haber: " + LogicaContable.cuentasHaber.get(i) + 
                                   " por C$ " + LogicaContable.importesMovimientos.get(i));
                enc = true;
            }
        }
        if (!enc) System.out.println("No hay transacciones en esa fecha.");
    }
}