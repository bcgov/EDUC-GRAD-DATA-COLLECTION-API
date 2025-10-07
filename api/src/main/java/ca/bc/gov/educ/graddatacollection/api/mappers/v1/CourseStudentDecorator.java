package ca.bc.gov.educ.graddatacollection.api.mappers.v1;

import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentEntity;
import ca.bc.gov.educ.graddatacollection.api.model.v1.CourseStudentLightEntity;
import ca.bc.gov.educ.graddatacollection.api.struct.v1.CourseStudent;

public abstract class CourseStudentDecorator implements CourseStudentMapper {

    private final CourseStudentMapper delegate;

    protected CourseStudentDecorator(CourseStudentMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public CourseStudent toCourseStudent(CourseStudentEntity courseStudentEntity) {
        return setDefaultNumberOfCredits(this.delegate.toCourseStudent(courseStudentEntity));
    }

    @Override
    public CourseStudent toCourseStudent(CourseStudentLightEntity courseStudentEntity) {
        return setDefaultNumberOfCredits(this.delegate.toCourseStudent(courseStudentEntity));
    }

    private CourseStudent setDefaultNumberOfCredits(CourseStudent courseStudent) {
        if (courseStudent.getNumberOfCredits() == null) {
            courseStudent.setNumberOfCredits("0");
        }
        return courseStudent;
    }
}

