package co.edu.unal.paralela;

import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * Clase que contiene los métodos para implementar la suma de los recíprocos de un arreglo usando paralelismo.
 */
public final class ReciprocalArraySum {

	/**
	 * Constructor.
	 */
	private ReciprocalArraySum() {}

	/**
	 * ForkJoinPool estático para reutilizar hilos y mejorar el desempeño en las pruebas repetitivas.
	 * Crear hilos es pesado asi que usamos un pool de hilos reutilizable.
	 */
	private static ForkJoinPool pool;

	/**
	 * Número de tareas en el pool.
	 */
	private static int poolTasks = -1;

	/**
	 * Obtiene el pool de tareas, creándolo si es necesario.
	 * @param n Número de tareas
	 * @return ForkJoinPool
	 */
	private static synchronized ForkJoinPool getPool(int n) {
		if (pool == null || poolTasks != n) {
			if (pool != null) {
				pool.shutdown();
			}

			// Crea un pool de tareas con n hilos de trabajo
			pool = new ForkJoinPool(n);
			poolTasks = n;
		}

		return pool;
	}

	/**
	 * Calcula secuencialmente la suma de valores recíprocos para un arreglo.
	 *
	 * @param input Arreglo de entrada
	 * @return La suma de los recíprocos del arreglo de entrada
	 */
	protected static double seqArraySum(final double[] input) {
		double sum = 0;

		// Calcula la suma de los recíprocos de los elementos del arreglo
		for (int i = 0; i < input.length; i++) {
			sum += 1 / input[i];
		}

		return sum;
	}

	/**
	 * calcula el tamaño de cada trozo o sección, de acuerdo con el número de secciones para crear
	 * a través de un número dado de elementos.
	 *
	 * @param nChunks El número de secciones (chunks) para crear
	 * @param nElements El número de elementos para dividir
	 * @return El tamaño por defecto de la sección (chunk)
	 */
	private static int getChunkSize(final int nChunks, final int nElements) {
		// Función techo entera
		return (nElements + nChunks - 1) / nChunks;
	}

	/**
	 * Calcula el índice del elemento inclusivo donde la sección/trozo (chunk) inicia,
	 * dado que hay cierto número de secciones/trozos (chunks).
	 *
	 * @param chunk la sección/trozo (chunk) para cacular la posición de inicio
	 * @param nChunks Cantidad de secciones/trozos (chunks) creados
	 * @param nElements La cantidad de elementos de la sección/trozo que deben atravesarse
	 * @return El índice inclusivo donde esta sección/trozo (chunk) inicia en el conjunto de
	 *         nElements
	 */
	private static int getChunkStartInclusive(
		final int chunk,
		final int nChunks,
		final int nElements
	) {
		final int chunkSize = getChunkSize(nChunks, nElements);

		return chunk * chunkSize;
	}

	/**
	 * Calcula el índice del elemento exclusivo que es proporcionado al final de la sección/trozo (chunk),
	 * dado que hay cierto número de secciones/trozos (chunks).
	 *
	 * @param chunk La sección para calcular donde termina
	 * @param nChunks Cantidad de secciones/trozos (chunks) creados
	 * @param nElements La cantidad de elementos de la sección/trozo que deben atravesarse
	 * @return El índice de terminación exclusivo para esta sección/trozo (chunk)
	 */
	private static int getChunkEndExclusive(
		final int chunk,
		final int nChunks,
		final int nElements
	) {
		final int chunkSize = getChunkSize(nChunks, nElements);
		final int end = (chunk + 1) * chunkSize;

		if (end > nElements) {
			return nElements;
		} else {
			return end;
		}
	}

	/**
	 * Este pedazo de clase puede ser completada para para implementar el cuerpo de cada tarea creada
	 * para realizar la suma de los recíprocos del arreglo en paralelo.
	 */
	private static class ReciprocalArraySumTask extends RecursiveAction {

		/**
		 * Iniciar el índice para el recorrido transversal hecho por esta tarea.
		 */
		private final int startIndexInclusive;
		/**
		 * Concluir el índice para el recorrido transversal hecho por esta tarea.
		 */
		private final int endIndexExclusive;
		/**
		 * Arreglo de entrada para la suma de recíprocos.
		 */
		private final double[] input;
		/**
		 * Valor intermedio producido por esta tarea.
		 */
		private double value;

		/**
		 * Constructor.
		 * @param setStartIndexInclusive establece el índice inicial para comenzar
		 *        el recorrido trasversal.
		 * @param setEndIndexExclusive establece el índice final para el recorrido trasversal.
		 * @param setInput Valores de entrada
		 */
		ReciprocalArraySumTask(
			final int setStartIndexInclusive,
			final int setEndIndexExclusive,
			final double[] setInput
		) {
			this.startIndexInclusive = setStartIndexInclusive;
			this.endIndexExclusive = setEndIndexExclusive;
			this.input = setInput;
		}

		/**
		 * Adquiere el valor calculado por esta tarea.
		 * @return El valor calculado por esta tarea
		 */
		public double getValue() {
			return value;
		}

		/**
		 * Calcula el valor de la suma de los recíprocos de los elementos del arreglo.
		 * Similar a seqArraySum, pero con un rango de elementos definido.
		 */
		@Override
		protected void compute() {
			double sum = 0;

			for (int i = startIndexInclusive; i < endIndexExclusive; i++) {
				sum += 1.0 / input[i];
			}

			value = sum;
		}
	}

	/**
	 * Se modifico este método para calcular la misma suma de recíprocos como la realizada en
	 * seqArraySum, pero utilizando dos tareas ejecutándose en paralelo dentro del framework ForkJoin de Java
	 * Se puede asumir que el largo del arreglo de entrada
	 * es igualmente divisible por 2.
	 *
	 * @param input Arreglo de entrada
	 * @return La suma de los recíprocos del arreglo de entrada
	 */
	protected static double parArraySum(final double[] input) {
		// Se asume que el largo del arreglo de entrada es divisible por 2
		assert input.length % 2 == 0;

		// Se crea una tarea para cada mitad del arreglo
		final ReciprocalArraySumTask left = new ReciprocalArraySumTask(0, input.length / 2, input);
		final ReciprocalArraySumTask right = new ReciprocalArraySumTask(
			input.length / 2,
			input.length,
			input
		);

		// Se inicia el cálculo paralelo del arreglo
		// getPool(2).invoke(
		// 	new RecursiveAction() {
		// 		/**
		// 		 * Combina las dos tareas en una sola tarea.
		// 		 */
		// 		@Override
		// 		protected void compute() {
		// 			left.fork(); // Divide el trabajo en dos tareas más pequeñas
		// 			right.compute();
		// 			left.join(); // Espera a que la tarea izquierda termine
		// 		}
		// 	}
		// );

		// Usar commonPool, este tiene el mismo número de hilos que los procesadores disponibles
		ForkJoinPool.commonPool().invoke(
			new RecursiveAction() {
				@Override
				protected void compute() {
					left.fork();
					right.compute();
					left.join();
				}
			}
		);

		return left.getValue() + right.getValue();
	}

	/**
	 * Se extendio el trabajo hecho para implementar parArraySum que permita utilizar un número establecido
	 * de tareas para calcular la suma del arreglo recíproco.
	 * getChunkStartInclusive y getChunkEndExclusive pueden ser útiles para calcular
	 * el rango de elementos índice que pertenecen a cada sección/trozo (chunk).
	 *
	 * @param input Arreglo de entrada
	 * @param numTasks El número de tareas para crear
	 * @return La suma de los recíprocos del arreglo de entrada
	 */
	protected static double parManyTaskArraySum(final double[] input, final int numTasks) {
		// Se crea un arreglo de tareas
		final ReciprocalArraySumTask[] tasks = new ReciprocalArraySumTask[numTasks];

		// Se crea una tarea para cada trozo del arreglo
		for (int i = 0; i < numTasks; i++) {
			tasks[i] = new ReciprocalArraySumTask(
				getChunkStartInclusive(i, numTasks, input.length),
				getChunkEndExclusive(i, numTasks, input.length),
				input
			);
		}

		// Inicia el cálculo paralelo del arreglo
		// getPool(numTasks).invoke(
		// 	new RecursiveAction() {
		// 		/**
		// 		 * invokeAll() divide el trabajo en tareas más pequeñas
		// 		 * Se computan las tareas en paralelo utilizando el pool de hilos
		// 		 */
		// 		@Override
		// 		protected void compute() {
		// 			invokeAll(tasks);
		// 		}
		// 	}
		// );

		// Usar commonPool, este tiene el número de hilos igual a los procesadores disponibles
		ForkJoinPool.commonPool().invoke(
			new RecursiveAction() {
				@Override
				protected void compute() {
					invokeAll(tasks);
				}
			}
		);

		double total = 0;

		// Se suma el valor de cada tarea
		for (int i = 0; i < numTasks; i++) {
			total += tasks[i].getValue();
		}

		return total;
	}
}
