package com.github.libgraviton.workerbase.helper;

import com.github.libgraviton.gdk.GravitonApi;
import com.github.libgraviton.gdk.api.Response;
import com.github.libgraviton.gdk.exception.CommunicationException;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatus;
import com.github.libgraviton.gdk.gravitondyn.eventstatus.document.EventStatusStatusAction;
import com.github.libgraviton.workerbase.exception.GravitonCommunicationException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Modify EventStatus entries at Graviton
 *
 * @author List of contributors
 *         https://github.com/libgraviton/graviton/graphs/contributors
 * @see <a href="http://swisscom.chm">http://swisscom.ch</a>
 */
public class EventStatusHandler {

    private static final Logger LOG = LoggerFactory.getLogger(EventStatusHandler.class);

    protected GravitonApi gravitonApi;

    public EventStatusHandler(GravitonApi gravitonApi) {
        this.gravitonApi = gravitonApi;
    }


    /**
     * Update the event status object on Graviton side
     *
     * @param eventStatus  adapted status that should be updated
     * @param workerId id of this worker
     * @param status the new status of the worker
     * @param action link to a worker action
     * @throws GravitonCommunicationException when status cannot be updated at Graviton
     */
    public void updateWithAction(EventStatus eventStatus, String workerId, EventStatusStatus.Status status, EventStatusStatusAction action) throws GravitonCommunicationException {
        EventStatusStatus workerStatus = new EventStatusStatus();
        workerStatus.setWorkerId(workerId);
        workerStatus.setStatus(status);
        workerStatus.setAction(action);
        update(eventStatus, workerStatus);
    }

    /**
     * Update the event status object on Graviton side
     *
     * @param eventStatus  adapted status that should be updated
     * @param workerId id of this worker
     * @param status the new status of the worker
     * @throws GravitonCommunicationException when status cannot be updated at Graviton
     */
    public void update(EventStatus eventStatus, String workerId, EventStatusStatus.Status status) throws GravitonCommunicationException {
        EventStatusStatus workerStatus = new EventStatusStatus();
        workerStatus.setWorkerId(workerId);
        workerStatus.setStatus(status);
        update(eventStatus, workerStatus);
    }

    protected void update(EventStatus eventStatus, EventStatusStatus workerStatus) throws GravitonCommunicationException {

        List<EventStatusStatus> status = eventStatus.getStatus();

        if (status == null || status.size() == 0) {
            throw new IllegalStateException("Got an invalid EventStatus status.");
        }

        for (EventStatusStatus statusEntry : status) {
            String currentWorkerId = statusEntry.getWorkerId();
            if (currentWorkerId != null && currentWorkerId.equals(workerStatus.getWorkerId())) {
                if(workerStatus.getAction() != null) {
                    statusEntry.setAction(workerStatus.getAction());
                }
                statusEntry.setStatus(workerStatus.getStatus());
                break;
            }
        }

        try {
            gravitonApi.patch(eventStatus).execute();
        } catch (CommunicationException e) {
            throw new GravitonCommunicationException("Failed to update the event status.", e);
        }

        LOG.debug("Updated /event/status/" + eventStatus.getId() + " to '" + workerStatus.getStatus() + "'.");
    }

    public EventStatus getEventStatusFromUrl(String url) throws GravitonCommunicationException {
        try {
            Response response = gravitonApi.get(url).execute();
            return response.getBodyItem(EventStatus.class);
        } catch (CommunicationException e) {
            throw new GravitonCommunicationException("Failed to GET event status from '" + url + "'.", e);
        }
    }

    /**
     * Retrieve an EventStatus by a certain RQL filter.
     *
     * @param filter RQL filter
     * @return EventStatus matching the criteria
     * @throws GravitonCommunicationException when there are less or more than 1 EventStatus match.
     */
    public EventStatus getEventStatusByFilter(String filter) throws GravitonCommunicationException {
        List<EventStatus> statusDocuments = findEventStatus(filter);
        int statusCount = statusDocuments.size();

        if (statusCount == 0) {
            throw new GravitonCommunicationException("No corresponding event status found for filter '" + filter + "'.");
        }

        if (statusCount > 1) {
            throw new GravitonCommunicationException("Multiple event status matches found for filter '" + filter + "'.");
        }

        return statusDocuments.get(0);
    }

    /**
     * Finds all event status documents which are found by a given RQL filter.
     *
     * @param filter RQL filter that gets attached to the eventStatusBaseUrl
     *
     * @return All found event status documents
     *
     * @throws GravitonCommunicationException when Event Status cannot be retrieved from Graviton
     */
    public List<EventStatus> findEventStatus(String filter) throws GravitonCommunicationException {
        String eventStatusEndpointUrl = gravitonApi
                .getEndpointManager()
                .getEndpoint(EventStatus.class.getName())
                .getUrl();

        try {
            Response response = gravitonApi.get(eventStatusEndpointUrl + filter).execute();
            return response.getBodyItems(EventStatus.class);
        } catch (CommunicationException e) {
            throw new GravitonCommunicationException("Could not GET matching EventStatus for filter '" + filter + "'.", e);
        }
    }

    /**
     * Creates an RQL filter by replacing all placeholder in the form {placeholder} from the rqlTemplate.
     *
     * @param rqlTemplate template with placeholders
     * @param replacements placeholder replacements
     * @return ready to use RQL filter
     */
    public String getRqlFilter(String rqlTemplate, String... replacements) {
        // placeholder -> {somePlaceholder}
        String placeholderRegex = "\\{[^\\{]*\\}";

        for (String replacement : replacements) {
            replacement = replacement.replaceAll("-", "%2D");
            rqlTemplate = rqlTemplate.replaceFirst(placeholderRegex, replacement);
        }
        return rqlTemplate;
    }
}
