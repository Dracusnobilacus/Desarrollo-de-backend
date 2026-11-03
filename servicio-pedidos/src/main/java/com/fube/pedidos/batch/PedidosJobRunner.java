package com.fube.pedidos.batch;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@ConditionalOnProperty(name = "spring.batch.job.enabled", havingValue = "true")
public class PedidosJobRunner implements CommandLineRunner {

    private final JobLauncher jobLauncher;
    private final Job expirarPedidosJob;

    public PedidosJobRunner(JobLauncher jobLauncher, Job expirarPedidosJob) {
        this.jobLauncher = jobLauncher;
        this.expirarPedidosJob = expirarPedidosJob;
    }

    @Override
    public void run(String... args) throws Exception {
        JobParameters params = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis())
                .toJobParameters();

        log.info("Lanzando expirarPedidosJob vía CommandLineRunner con timestamp={}", params.getLong("timestamp"));
        jobLauncher.run(expirarPedidosJob, params);
    }
}

