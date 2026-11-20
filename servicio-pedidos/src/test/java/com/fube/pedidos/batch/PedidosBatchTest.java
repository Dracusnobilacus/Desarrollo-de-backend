package com.fube.pedidos.batch;

import com.fube.pedidos.clientes.InventarioClient;
import com.fube.pedidos.modelos.EstadoPedido;
import com.fube.pedidos.modelos.Pedido;
import com.fube.pedidos.repositorio.PedidoRepositorio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobInstance;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:pedidosbatchtest;MODE=PostgreSQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect",
        "spring.flyway.enabled=false",
        "spring.batch.jdbc.initialize-schema=always",
        "spring.batch.job.enabled=true"
})
class PedidosBatchTest {

    @Autowired
    private JobExplorer jobExplorer;

    @Autowired
    private JobLauncher jobLauncher;

    @Autowired
    private Job expirarPedidosJob;

    @Autowired
    private PedidoRepositorio pedidoRepositorio;

    @MockBean
    private InventarioClient inventarioClient;

    @Test
    @DisplayName("Al iniciar con spring.batch.job.enabled=true el job se ejecuta una sola vez vía CommandLineRunner y procesa múltiples chunks sin saltear pedidos")
    void batchExpirarPedidos_ejecutaUnaVezAlInicioYProcesaMultiplesChunks() throws Exception {
        // 1. Verificar que al arrancar con spring.batch.job.enabled=true se ejecutó exactamente 1 vez (sin doble ejecución)
        List<JobInstance> instanciasAlInicio = jobExplorer.getJobInstances("expirarPedidosJob", 0, 10);
        assertEquals(1, instanciasAlInicio.size());

        // 2. Insertar 15 pedidos CREADOS vencidos (>24hs, más de 1 chunk de 10), 2 recientes y 1 CONFIRMADO vencido
        when(inventarioClient.liberarReservasPorPedido(anyLong())).thenReturn(true);

        LocalDateTime hace30Horas = LocalDateTime.now().minusHours(30);
        LocalDateTime hace2Horas = LocalDateTime.now().minusHours(2);

        for (int i = 0; i < 15; i++) {
            pedidoRepositorio.save(Pedido.builder()
                    .clienteId(1L)
                    .estado(EstadoPedido.CREADO)
                    .montoTotal(new BigDecimal("100.00"))
                    .fechaCreacion(hace30Horas)
                    .fechaActualizacion(hace30Horas)
                    .build());
        }
        for (int i = 0; i < 2; i++) {
            pedidoRepositorio.save(Pedido.builder()
                    .clienteId(1L)
                    .estado(EstadoPedido.CREADO)
                    .montoTotal(new BigDecimal("200.00"))
                    .fechaCreacion(hace2Horas)
                    .fechaActualizacion(hace2Horas)
                    .build());
        }
        pedidoRepositorio.save(Pedido.builder()
                .clienteId(1L)
                .estado(EstadoPedido.CONFIRMADO)
                .montoTotal(new BigDecimal("300.00"))
                .fechaCreacion(hace30Horas)
                .fechaActualizacion(hace30Horas)
                .build());

        // 3. Ejecutar el job
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis() + 1000)
                .toJobParameters();
        JobExecution execution = jobLauncher.run(expirarPedidosJob, params);

        assertEquals(BatchStatus.COMPLETED, execution.getStatus());
        assertEquals(15, pedidoRepositorio.findByEstado(EstadoPedido.EXPIRADO).size());
        assertEquals(2, pedidoRepositorio.findByEstado(EstadoPedido.CREADO).size());
        assertEquals(1, pedidoRepositorio.findByEstado(EstadoPedido.CONFIRMADO).size());
        verify(inventarioClient, times(15)).liberarReservasPorPedido(anyLong());
    }
}
