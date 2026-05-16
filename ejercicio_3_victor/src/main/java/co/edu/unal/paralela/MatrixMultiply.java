package co.edu.unal.paralela;

import static edu.rice.pcdp.PCDP.forseq2d;
import static edu.rice.pcdp.PCDP.forall2dChunked;

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
     * Realiza una multiplicación de matrices bidimensionales (A x B = C) de forma
     * paralela.
     *
     * @param A Una matriz de entrada con dimensiones NxN
     * @param B Una matriz de entrada con dimensiones NxN
     * @param C Matriz de salida
     * @param N tamaño de las matrices de entrada
     */
    public static void parMatrixMultiply(double[][] A, double B[][], double[][] C, int n) {
        // // Implementacion 1: forall2d (x3)
        // forall2d(0, n - 1, 0, n - 1, (i, j) -> {
        // C[i][j] = 0;
        // for (int k = 0; k < n; k++) {
        // C[i][j] += A[i][k] * B[k][j];
        // }
        // });

        // // Implementacion 2: Con acumulador local (x4)
        // forall2dChunked(0, n - 1, 0, n - 1, (i, j) -> {
        // double sum = 0;

        // for (int k = 0; k < n; k++) {
        // sum += A[i][k] * B[k][j];
        // }

        // C[i][j] = sum;
        // });

        // Implementacion 3: Reordenar bucles i, k, j (x24)
        forall2dChunked(0, n - 1, 0, n - 1, (i, k) -> {
            double temp = A[i][k];

            for (int j = 0; j < n; j++) {
                C[i][j] += temp * B[k][j];
            }
        });
    }
}
