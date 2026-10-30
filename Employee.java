package ru.payroll;

import java.util.Objects;

/**
 * Сотрудник. Идентификация по табельному номеру.
 */
public final class Employee {

    private final String id;
    private final String fullName;

    /**
     * @param id       табельный номер (не пустой)
     * @param fullName ФИО
     */
    public Employee(String id, String fullName) {
        this.id = Objects.requireNonNull(id, "id").strip();
        if (this.id.isEmpty()) {
            throw new IllegalArgumentException("id must not be blank");
        }
        this.fullName = Objects.requireNonNull(fullName, "fullName").strip();
        if (this.fullName.isEmpty()) {
            throw new IllegalArgumentException("fullName must not be blank");
        }
    }

    /** @return табельный номер */
    public String getId() {
        return id;
    }

    /** @return ФИО */
    public String getFullName() {
        return fullName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Employee other)) {
            return false;
        }
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return fullName + " (" + id + ")";
    }
}
