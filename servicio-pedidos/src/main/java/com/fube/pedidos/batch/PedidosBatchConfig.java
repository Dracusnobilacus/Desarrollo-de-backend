package com.fube.pedidos.batch;

import com.fube.pedidos.modelos.Pedido;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class PedidosBatchConfig {

    @Bean
    public Step expirarPedidosStep(JobRepository jobRepository,
                                   PlatformTransactionManager transactionManager,
                                   PedidosItemReader reader,
                                   PedidosItemProcessor processor,
                                   PedidosItemWriter writer) {
        return new StepBuilder("expirarPedidosStep", jobRepository)
                .<Pedido, Pedido>chunk(10, transactionManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job expirarPedidosJob(JobRepository jobRepository, Step expirarPedidosStep) {
        return new JobBuilder("expirarPedidosJob", jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(jobExecutionListener())
                .start(expirarPedidosStep)
                .build();
    }

    private JobExecutionListener jobExecutionListener() {
        return new JobExecutionListener() {
            @Override
            public void beforeJob(JobExecution jobExecution) {
                log.info("=== Iniciando job: {} ===", jobExecution.getJobInstance().getJobName());
            }

            @Override
            public void afterJob(JobExecution jobExecution) {
                long totalLeidos = jobExecution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getReadCount)
                        .sum();
                long totalExpirados = jobExecution.getStepExecutions().stream()
                        .mapToLong(StepExecution::getWriteCount)
                        .sum();

                log.info("==================================================");
                log.info("=== RESUMEN DE LA CORRIDA DEL JOB: {} ===", jobExecution.getJobInstance().getJobName());
                log.info("=== Estado: {}", jobExecution.getStatus());
                log.info("=== Pedidos leídos (>24hs sin confirmar): {}", totalLeidos);
                log.info("=== Pedidos expirados y stock liberado: {}", totalExpirados);
                log.info("=== Inicio: {} | Fin: {}", jobExecution.getStartTime(), jobExecution.getEndTime());
                log.info("==================================================");
            }
        };
    }
}
