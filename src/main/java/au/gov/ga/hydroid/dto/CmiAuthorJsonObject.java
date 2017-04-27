package au.gov.ga.hydroid.dto;

import java.util.List;

/**
 * A helper Json object to retrieve cmi node author.
 */
public class CmiAuthorJsonObject {
    private List<CmiSimpleJsonObject> field_principal_contributors;

    public List<CmiSimpleJsonObject> getPrincipalContributors() {
        return field_principal_contributors;
    }

    public void setPrincipalContributors(List<CmiSimpleJsonObject> principalContributors) {
        this.field_principal_contributors = principalContributors;
    }
}
