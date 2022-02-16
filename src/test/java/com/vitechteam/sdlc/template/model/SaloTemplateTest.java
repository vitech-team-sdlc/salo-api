package com.vitechteam.sdlc.template.model;

import com.vitechteam.sdlc.env.model.rest.SaloInput;
import com.vitechteam.sdlc.testConfig.SmallTest;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class SaloTemplateTest {

    @SmallTest
    void testTemplateMapsToSaloInput() {
        final SaloTemplateToInputMapper mapper = Mappers.getMapper(SaloTemplateToInputMapper.class);

        // TODO: for now, it's enough see that mapstruct successfully generated mapping,
        //       testing how actual mapping is ran can be useful later
        final SaloInput saloInput = mapper.mapSalo(SaloTemplate.builder().build());

        assertThat(saloInput).isNotNull();
    }
}