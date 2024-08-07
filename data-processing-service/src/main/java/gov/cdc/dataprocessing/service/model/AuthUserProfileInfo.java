package gov.cdc.dataprocessing.service.model;

import gov.cdc.dataprocessing.repository.nbs.odse.model.AuthUserRealizedRole;
import gov.cdc.dataprocessing.repository.nbs.odse.model.auth.AuthUser;
import lombok.Getter;
import lombok.Setter;

import java.util.Collection;

@Getter
@Setter
public class AuthUserProfileInfo {
    private AuthUser authUser;
    private Collection<AuthUserRealizedRole> authUserRealizedRoleCollection;
}
