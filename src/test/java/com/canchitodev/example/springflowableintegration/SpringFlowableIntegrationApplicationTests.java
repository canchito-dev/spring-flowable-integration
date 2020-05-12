/**
 * This content is released under the MIT License (MIT)
 *
 * Copyright (c) 2020, canchito-dev
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *
 * @author 		José Carlos Mendoza Prego
 * @copyright	Copyright (c) 2020, canchito-dev (http://www.canchito-dev.com)
 * @license		http://opensource.org/licenses/MIT	MIT License
 * @link		http://www.canchito-dev.com/public/blog/2020/05/10/integrate-flowable-into-your-spring-boot-application/
 * @link		https://github.com/canchito-dev/spring-flowable-integration
 **/
package com.canchitodev.example.springflowableintegration;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.test.Deployment;
import org.flowable.task.api.Task;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest
class SpringFlowableIntegrationApplicationTests {

	@Autowired
	private RuntimeService runtimeService;

	@Autowired
	private TaskService taskService;

	@Autowired
	private HistoryService historyService;

	@Test
	@Deployment(resources = "processes/one-task-process.bpmn20.bpmn")
	void simpleTestIncludingTaskVariables() {
		// Start a new process instance
		ProcessInstance processInstance = this.runtimeService.startProcessInstanceByKey("oneTaskProcess");

		// Wait until the engine has advance to the user task which id is "theTask"
		await().atMost(30L, TimeUnit.SECONDS).until(
				() -> this.runtimeService.createExecutionQuery()
						.activityId("theTask")
						.processInstanceId(processInstance.getProcessInstanceId())
						.singleResult() != null
		);

		// Get the task from the TaskService
		Task task = this.taskService.createTaskQuery()
				.processInstanceId(processInstance.getProcessInstanceId())
				.taskName("my task")
				.singleResult();

		// Make sure we got it
		assertThat(task).isNotNull();

		// Create some variables that will be submitted when the task is completed.
		Map<String, Object> taskVariables = new HashMap<String, Object>();
		taskVariables.put("form_outcome", "retry");

		// Complete the user task
		this.taskService.complete(task.getId(), taskVariables);

		// Query the HistoryService to make sure the variable was correctly submitted
		HistoricVariableInstance historicVariableInstance = this.historyService.createHistoricVariableInstanceQuery()
				.processInstanceId(processInstance.getProcessInstanceId())
				.variableName("form_outcome")
				.singleResult();

		// Do the variable verification
		assertThat(historicVariableInstance.getVariableName()).isEqualTo("form_outcome");
		assertThat(historicVariableInstance.getValue()).isEqualTo("retry");

		// Make sure the process has ended
		await().atMost(30L, TimeUnit.SECONDS).until(
				() -> this.historyService.createHistoricProcessInstanceQuery()
						.processInstanceId(processInstance.getProcessInstanceId())
						.finished()
						.singleResult() != null
		);
	}
}