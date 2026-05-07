package co.edu.unal.paralela;

import static edu.rice.pcdp.PCDP.forseq2d;

import java.util.stream.IntStream;

import edu.rice.pcdp.ProcedureInt2D;

import static edu.rice.pcdp.PCDP.async;
import static edu.rice.pcdp.PCDP.finish;
import static edu.rice.pcdp.PCDP.forall;
import static edu.rice.pcdp.PCDP.forall2d;

/**
 * Clase envolvente pata implementar de forma eficiente la multiplicación dde
 * matrices en paralelo.
 */
public final class MatrixMultiply {
    /**
     * Constructor por omisión.
     */
    private MatrixMultiply() {
    }

    /**
     * Realiza una multiplicación de matrices bidimensionales (A x B = C) de forma
     * secuencial.
     *
     * @param A Una matriz de entrada con dimensiones NxN
     * @param B Una matriz de entrada con dimensiones NxN
     * @param C Matriz de salida
     * @param N Tamaño de las matrices de entrada
     */
    public static void seqMatrixMultiply(final double[][] A, final double[][] B,
            final double[][] C, final int N) {
        forseq2d(0, N - 1, 0, N - 1, (i, j) -> {
            C[i][j] = 0.0;
            for (int k = 0; k < N; k++) {
                C[i][j] += A[i][k] * B[k][j];
            }
        });
    }

    /**
     * Realiza una paralelizacion de bucles 2d
     *
     * @param N1   Tamaño del bucle externo
     * @param N2   Tamaño del bucle interno
     * @param body Cuerpo del bucle
     */
    public static void parStreamForAll2d(final int N1, final int N2, final ProcedureInt2D body) {
        // IntStream.range(0, N1).parallel().forEach(i -> {
        // IntStream.range(0, N2).parallel().forEach(j -> body.apply(i, j));
        // });

        forall(0, N1 - 1, i -> forall(0, N2 - 1, j -> body.apply(i, j)));
    }

    /**
     * Realiza una multiplicación de matrices bidimensionales (A x B = C) de forma
     * paralela.
     *
     * @param A Una matriz de entrada con dimensiones NxN
     * @param B Una matriz de entrada con dimensiones NxN
     * @param C Matriz de salida
     * @param N amaño de las matrices de entrada
     */
    public static void parMatrixMultiply(double[][] A, double B[][], double[][] C, int n) {
        parStreamForAll2d(n, n, (i, j) -> {
            C[i][j] = 0;
            // for (int k = 0; k < n; k++) {
            // C[i][j] += A[i][k] * B[k][j];
            // }
            forall(0, n - 1, k -> C[i][j] += A[i][k] * B[k][j]);
        });
    }
}
