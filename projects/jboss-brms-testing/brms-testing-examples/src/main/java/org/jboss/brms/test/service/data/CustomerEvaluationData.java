package org.jboss.brms.test.service.data;

import java.util.HashMap;
import java.util.Map;

import org.jbpm.evaluation.customer.Person;
import org.jbpm.evaluation.customer.Request;

public class CustomerEvaluationData extends AbstractDataProvider {
    private static final String PROCESS_ID = "org.jbpm.customer-evaluation";

    /** {@inheritDoc} */
    @Override
    public String getProcessId() {
        return PROCESS_ID;
    }

    /** {@inheritDoc} */
    @Override
    public Map<String, Object> getStartParameters() {
        final Map<String, Object> params = new HashMap<String, Object>();
        final Person person = new Person("pid", "John Doe");
        person.setAge(21);
        params.put("person", person);
        final Request request = new Request("rid");
        request.setPersonId(person.getId());
        request.setAmount(5000);
        params.put("request", request);
        return params;
    }
}
