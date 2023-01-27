package com.example.workflow.delegates;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.logging.Logger;

@Component
public class GeneratePCSIDDelegate implements JavaDelegate {
    private final Logger logger = Logger.getLogger(GeneratePCSIDDelegate.class.getName());

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        logger.info("generating pcsid");
    }
}
