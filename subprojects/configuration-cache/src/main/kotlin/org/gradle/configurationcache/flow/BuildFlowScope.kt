/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.configurationcache.flow

import org.gradle.api.Action
import org.gradle.api.NonExtensible
import org.gradle.api.flow.FlowAction
import org.gradle.api.flow.FlowActionSpec
import org.gradle.api.flow.FlowParameters
import org.gradle.api.flow.FlowProviders
import org.gradle.api.flow.FlowScope
import org.gradle.api.internal.GradleInternal
import org.gradle.configurationcache.extensions.uncheckedCast
import org.gradle.initialization.internal.InternalBuildFinishedListener
import org.gradle.internal.instantiation.InstantiatorFactory
import org.gradle.internal.isolated.IsolationScheme
import org.gradle.internal.service.ServiceRegistry
import org.gradle.internal.service.scopes.Scopes
import org.gradle.internal.service.scopes.ServiceScope
import java.util.Optional
import javax.inject.Inject


@NonExtensible
@ServiceScope(Scopes.Build::class)
internal
open class BuildFlowScope @Inject constructor(
    private val flowScheduler: FlowScheduler,
    private val flowProviders: FlowProviders,
    instantiatorFactory: InstantiatorFactory,
    services: ServiceRegistry
) : FlowScope, InternalBuildFinishedListener {

    private
    val isolationScheme by lazy {
        IsolationScheme(
            FlowAction::class.java,
            FlowParameters::class.java,
            FlowParameters.None::class.java
        )
    }

    private
    val paramsInstantiator by lazy {
        instantiatorFactory.decorateScheme().withServices(services).instantiator()
    }

    private
    val specInstantiator by lazy {
        instantiatorFactory.decorateLenientScheme().withServices(services).instantiator()
    }

    sealed class State {

        abstract val pendingActions: List<RegisteredFlowAction>

        open fun add(registeredFlowAction: RegisteredFlowAction): Unit = illegalState()

        open fun store(): Pair<Any, State> = illegalState()

        open fun load(memento: Any): State {
            return Loaded(memento.uncheckedCast())
        }

        protected
        fun illegalState(): Nothing = throw IllegalStateException("This operation is not supported while in the ${javaClass.simpleName} state.")

        class Initial() : State() {

            private
            val actions = mutableListOf<RegisteredFlowAction>()

            override val pendingActions: List<RegisteredFlowAction>
                get() = actions

            override fun add(registeredFlowAction: RegisteredFlowAction) {
                synchronized(actions) {
                    actions.add(registeredFlowAction)
                }
            }

            override fun store(): Pair<Any, State> {
                return actions to Stored(actions)
            }
        }

        class Stored(override val pendingActions: List<RegisteredFlowAction>) : State()

        class Loaded(override val pendingActions: List<RegisteredFlowAction>) : State() {

            override fun load(memento: Any): State = illegalState()
        }
    }

    private
    var state: State = State.Initial()

    fun store(): Any {
        val (memento, newState) = state.store()
        state = newState
        return memento
    }

    fun load(memento: Any) {
        state = state.load(memento)
    }

    override fun buildFinished(gradle: GradleInternal, failure: Throwable?) {
        setRequestedTasksResult(failure)
        flowScheduler.schedule(state.pendingActions)
    }

    override fun <P : FlowParameters> always(
        action: Class<out FlowAction<P>>,
        configure: Action<in FlowActionSpec<P>>
    ): FlowScope.Registration<P> {
        val parameters = configureParametersFor(action, configure)

        val registeredFlowAction = RegisteredFlowAction(action.uncheckedCast(), parameters)
        state.add(registeredFlowAction)
        return DefaultFlowScopeRegistration()
    }

    private
    fun <P : FlowParameters> configureParametersFor(
        action: Class<out FlowAction<P>>,
        configure: Action<in FlowActionSpec<P>>
    ): P? = parametersTypeOf(action)?.let { parametersType ->
        val parameters = paramsInstantiator.newInstance(parametersType)
        val spec = specInstantiator.newInstance(DefaultFlowActionSpec::class.java, parameters)
        configure.execute(spec.uncheckedCast())
        parameters
    }

    private
    fun <P : FlowParameters, T : FlowAction<P>> parametersTypeOf(action: Class<T>): Class<P>? =
        isolationScheme.parameterTypeFor(action)

    private
    fun setRequestedTasksResult(failure: Throwable?) {
        flowProviders.requestedTasksResult.uncheckedCast<RequestedTasksResultProvider>().apply {
            set { Optional.ofNullable(failure) }
        }
    }
}


internal
data class RegisteredFlowAction(
    val type: Class<out FlowAction<FlowParameters>>,
    val parameters: FlowParameters?
)


private
class DefaultFlowScopeRegistration<P : FlowParameters> : FlowScope.Registration<P>


@NonExtensible
private
open class DefaultFlowActionSpec<P : FlowParameters>(
    private val parameters: P
) : FlowActionSpec<P> {

    override fun getParameters(): P =
        parameters

    override fun toString(): String =
        "FlowActionSpec($parameters)"
}
