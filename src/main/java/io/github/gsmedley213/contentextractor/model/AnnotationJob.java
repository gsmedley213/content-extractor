package io.github.gsmedley213.contentextractor.model;

import java.util.List;

public record AnnotationJob(String bookPrefix, int annotationRun, String description, List<Notable> notables) {
}
