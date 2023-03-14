package com.advanced.restapi.lectures;

import java.time.LocalDateTime;

import com.advanced.restapi.lectures.dto.LectureReqDto;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

@Component
public class LectureValidator {
	public void validate(LectureReqDto lectureReqDto, Errors errors) {
		//MaxPrice가 있다면 basePrice > maxPrice 보다 더 크면 오류 발생
		if(lectureReqDto.getBasePrice() > lectureReqDto.getMaxPrice() &&
				lectureReqDto.getMaxPrice() != 0) {
			//Field Error
			errors.rejectValue("basePrice", "wrongValue", "BasePrice가 MaxPrice 보다 더 작아야 합니다.");
			errors.rejectValue("maxPrice", "wrongValue", "MaxPrice는 BasePrice 보다 더 커야 합니다.");
			//Global Error
			errors.reject("wrongPrices", "Values for prices are wrong");
		}

		//강의 등록 시작/종료 일자와 강의시작 일자가 강의 종료일자 보다 더 이후의 날짜이면 오류 발생
		LocalDateTime endLectureDateTime = lectureReqDto.getEndLectureDateTime();
		if(endLectureDateTime.isBefore(lectureReqDto.getBeginLectureDateTime()) ||
		   endLectureDateTime.isBefore(lectureReqDto.getCloseEnrollmentDateTime()) ||
		   endLectureDateTime.isBefore(lectureReqDto.getBeginEnrollmentDateTime()) ) {
			errors.rejectValue("endLectureDateTime", "wrongValue", "endLectureDateTime is wrong");
		}
	}
}