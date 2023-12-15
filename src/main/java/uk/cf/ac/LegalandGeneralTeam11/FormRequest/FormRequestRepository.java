package uk.cf.ac.LegalandGeneralTeam11.FormRequest;

import java.util.List;

public interface FormRequestRepository {
    /**
     * Save a form request.
     *
     * @param formRequest The form request to be saved.
     */
   // void saveFormRequest(FormRequest formRequest);

    /**
     * Get a list of all form requests.
     *
     * @return List of form requests.
     */
    List<FormRequest> getAllFormRequests();
    /**
     * Get a form request by its ID.
     *
     * @param formId The ID of the form request.
     * @return The form request with the specified ID, or null if not found.
     */
    FormRequest getFormRequestById(Long formId);

    /**
     * Get a list of all form requests.
     * @param username The ID of the user.
     *
     * @return List of form requests for a specific user.
     */

    List<FormRequest> getAllByUser(String username);
    /**
     * Create a form request.
     *
     * @param formRequest The form request to be created.
     */
    void createFormRequest(FormRequest formRequest);

    /**
     * Get a list of all form requests.
     * @param username The username of the user.
     *
     * @return List of form requests for a specific user.
     */

    List<FormRequest> findPendingRequestsByUsername(String username);

    /**
     * getting a form request by its id
     * @param formRequestId
     * @return
     */


    FormRequest findFormRequestById(Long formRequestId);

    /**
     * updating a form request
     * @param formRequest
     */

    void updateFormRequest(FormRequest formRequest);
    /**
     * Get a list of all form requests by status.
     */

    List<FormRequest> getAllByStatus(String status);

    public void rejectFormRequest(FormRequest formRequest);


}
