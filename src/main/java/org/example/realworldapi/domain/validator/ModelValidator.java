package org.example.realworldapi.domain.validator;

import lombok.AllArgsConstructor;
import org.example.realworldapi.domain.exception.ModelValidationException;

import javax.inject.Singleton;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
@AllArgsConstructor
public class ModelValidator {

  private final Validator validator;

  public <T> T validate(T model) {
    Set<ConstraintViolation<T>> constraintViolations = validator.validate(model);

    if (!constraintViolations.isEmpty()) {
      final var messages =
          constraintViolations.stream()
              .map(ConstraintViolation::getMessage)
              .collect(Collectors.toList());
      throw new ModelValidationException(messages);
    }

    return model;
  }
}
