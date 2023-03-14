package com.advanced.restapi.lectures;


import com.advanced.restapi.common.ErrorsResource;
import com.advanced.restapi.lectures.dto.LectureReqDto;
import com.advanced.restapi.lectures.dto.LectureResDto;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedResourcesAssembler;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.hateoas.PagedModel;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.net.URI;
import java.util.Optional;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Controller
@RequestMapping(value="/api/lectures", produces = MediaTypes.HAL_JSON_VALUE)
@RequiredArgsConstructor
public class LectureController {
    private final LectureRepository lectureRepository;
    private final ModelMapper modelMapper;
    private final LectureValidator lectureValidator;

    @PutMapping("/{id}")
    public ResponseEntity updateLecture(@PathVariable Integer id,
                                        @RequestBody @Valid LectureReqDto lectureReqDto,
                                        Errors errors) {
        Optional<Lecture> optionalLecture = lectureRepository.findById(id);
        if (optionalLecture.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(id +" Lecture Not Found!!");
        }
        //입력항목 체크
        if (errors.hasErrors()) {
            return badRequest(errors);
        }
        //입력항목의 biz logic 체크
        lectureValidator.validate(lectureReqDto, errors);
        if (errors.hasErrors()) {
            return badRequest(errors);
        }
        //Option<Lecture> => Lecture
        Lecture existingLecture = optionalLecture.get();
        //LectureReqDto => Lecture
        this.modelMapper.map(lectureReqDto, existingLecture);
        //수정처리
        Lecture savedLecture = this.lectureRepository.save(existingLecture);
        //수정된 Lecture => LectureResDto
        LectureResDto lectureResDto = modelMapper.map(savedLecture, LectureResDto.class);

        //LectureResDto => LectureResource
        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping("/{id}")
    public ResponseEntity getLecture(@PathVariable Integer id) {
        Optional<Lecture> optionalLecture = this.lectureRepository.findById(id);
        if(optionalLecture.isEmpty()) {
            //return ResponseEntity.notFound().build();
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(id +" Lecture Not Found!!");
        }
        Lecture lecture = optionalLecture.get();
        LectureResDto lectureResDto = modelMapper.map(lecture, LectureResDto.class);
        LectureResource lectureResource = new LectureResource(lectureResDto);
        return ResponseEntity.ok(lectureResource);
    }

    @GetMapping
    public ResponseEntity queryLectures(Pageable pageable, PagedResourcesAssembler<LectureResDto> assembler) {
        Page<Lecture> lecturePage = this.lectureRepository.findAll(pageable);
        // Page<Lecture> => Page<LectureResDto>
        //<U> Page<U> map(Function<? super T,? extends U> converter)
        Page<LectureResDto> lectureResDtoPage =
                lecturePage.map(lecture -> modelMapper.map(lecture, LectureResDto.class));
        //public org.springframework.hateoas.PagedModel<org.springframework.hateoas.EntityModel<T>> toModel(Page<T> entity)
        //1단계 : first/prev/next/last 링크 포함
        //PagedModel<EntityModel<LectureResDto>> pagedModel = assembler.toModel(lectureResDtoPage);

        //2단계 : first/prev/next/last 링크 + 개별 엘리먼트의 셀프 링크 포함
        /*
        public <R extends org.springframework.hateoas.RepresentationModel<?>>
            org.springframework.hateoas.PagedModel<R> toModel(Page<T> page,
                org.springframework.hateoas.server.RepresentationModelAssembler<T,R> assembler)
         */
        PagedModel<LectureResource> pagedModel =
                assembler.toModel(lectureResDtoPage, lectureResDto -> new LectureResource(lectureResDto));
        return ResponseEntity.ok(pagedModel);
    }


    @PostMapping
    public ResponseEntity createLecture(@RequestBody @Valid LectureReqDto lectureReqDto,
                                        Errors errors) {
        //입력항목 체크
        if(errors.hasErrors()) {
            return badRequest(errors);
        }

        //입력항목의 biz logic 체크
        this.lectureValidator.validate(lectureReqDto, errors);
        if(errors.hasErrors()) {
            return badRequest(errors);
        }

        // LectureReqDto => Lecture 타입으로 매핑하기
        Lecture lecture = modelMapper.map(lectureReqDto, Lecture.class);

        //free 와 offline 속성 update
        lecture.update();

        Lecture addLecture = this.lectureRepository.save(lecture);
        LectureResDto lectureResDto = modelMapper.map(addLecture, LectureResDto.class);

        WebMvcLinkBuilder selfLinkBuilder =
                linkTo(LectureController.class).slash(lecture.getId());
                // http://localhost:8080/api/lectures/10
        URI createUri = selfLinkBuilder.toUri();

        LectureResource lectureResource = new LectureResource(lectureResDto);
        lectureResource.add(linkTo(LectureController.class).withRel("query-lectures"));
        lectureResource.add(selfLinkBuilder.withRel("update-lecture"));

        return ResponseEntity.created(createUri).body(lectureResource);
    }

    private static ResponseEntity<ErrorsResource> badRequest(Errors errors) {
        return ResponseEntity.badRequest().body(new ErrorsResource(errors));
    }
}
