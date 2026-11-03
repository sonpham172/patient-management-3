package org.study.patientservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.study.patientservice.dto.PatientRequestDTO;
import org.study.patientservice.dto.PatientResponseDTO;
import org.study.patientservice.exception.ExistedEmailExceptionHandler;
import org.study.patientservice.exception.PatientNotFoundException;
import org.study.patientservice.grpc.BillingServiceGrpcClient;
import org.study.patientservice.kafka.KafkaProducer;
import org.study.patientservice.mapper.PatientMapper;
import org.study.patientservice.model.Patient;
import org.study.patientservice.repository.PatientRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class PatientService {
    private static final Logger log = LoggerFactory.getLogger(PatientService.class);
    private final PatientRepository patientRepository;
    private final BillingServiceGrpcClient billingServiceGrpcClient;
    private final KafkaProducer kafkaProducer;

    public PatientService(PatientRepository patientRepository, BillingServiceGrpcClient billingServiceGrpcClient,
                          KafkaProducer kafkaProducer) {
        this.billingServiceGrpcClient = billingServiceGrpcClient;
        this.patientRepository = patientRepository;
        this.kafkaProducer = kafkaProducer;
    }

    public List<PatientResponseDTO> getPatient() {
        List<Patient> patients = patientRepository.findAll();

        return patients.stream().map(PatientMapper::toDTO).toList();
    }

    public PatientResponseDTO createPatient(PatientRequestDTO patientRequestDTO) {
        if(patientRepository.existsByEmail(patientRequestDTO.getEmail())) {
            throw new ExistedEmailExceptionHandler("Email " + patientRequestDTO.getEmail() +  " is already existed");
        }

        Patient createdPatient = patientRepository.save(PatientMapper.toModel(patientRequestDTO));

        billingServiceGrpcClient.createBillingAccount(createdPatient.getId().toString()
        ,createdPatient.getName(), createdPatient.getEmail());

        kafkaProducer.sendEvent(createdPatient);

        return PatientMapper.toDTO(createdPatient);
    }

    public PatientResponseDTO updatePatient(UUID id, PatientRequestDTO patientRequestDTO) {
        Patient targetPatientEntity = patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found!"));

        targetPatientEntity.setDateOfBirth(LocalDate.parse(patientRequestDTO.getDateOfBirth()));
        targetPatientEntity.setAddress(patientRequestDTO.getAddress());
        targetPatientEntity.setName(patientRequestDTO.getName());

        Patient updatedPatient = patientRepository.save(targetPatientEntity);
        return PatientMapper.toDTO(updatedPatient);
    }

    public void deletePatient(UUID id) {
        patientRepository.findById(id).orElseThrow(() -> new PatientNotFoundException("Patient not found!"));
        patientRepository.deleteById(id);
    }
}
