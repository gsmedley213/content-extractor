package io.github.gsmedley213.contentextractor.model;

import java.util.List;

public record AnnotationJob(long bookId, int jobId, String description, List<Notable> notables) {
}
