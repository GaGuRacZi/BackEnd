package com.gaguraczi.paw.domain.mypage.dto.req;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class DndWindowPairValidator implements ConstraintValidator<DndWindowPair, NotificationSettingUpdateReq> {

    @Override
    public boolean isValid(NotificationSettingUpdateReq req, ConstraintValidatorContext context) {
        if (req == null) {
            return true;
        }
        return (req.dndStart() == null) == (req.dndEnd() == null);
    }
}
