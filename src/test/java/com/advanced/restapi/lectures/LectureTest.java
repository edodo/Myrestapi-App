package com.advanced.restapi.lectures;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

class LectureTest {
    @Test
    public void builder() {
        Lecture lecture = Lecture.builder()//LectureBuilder static Inner Class 타입
                .name("Spring REST API")
                .description("REST API developmemt with Spring")
                .build(); //Lecture
        assertEquals("Spring REST API", lecture.getName());
    }
    @Test
    public void javaBean()
    {
        String name = "Lecture"; //Give
        String description = "Spring";
        Lecture lecture = new Lecture(); //When
        lecture.setName(name);
        lecture.setDescription(description);
        assertThat(lecture.getName()).isEqualTo("Lecture"); //Then
        assertThat(lecture.getDescription()).isEqualTo("Spring");
    }

}