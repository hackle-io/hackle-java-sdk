//package io.hackle.sdk.core.evaluation.evaluator.inappmessage
//
//import io.hackle.sdk.common.decision.DecisionReason
//import io.hackle.sdk.core.evaluation.evaluator.Evaluator
//import io.hackle.sdk.core.evaluation.evaluator.Evaluators
//import io.hackle.sdk.core.model.InAppMessage
//import io.mockk.mockk
//import org.junit.jupiter.api.Test
//import strikt.api.expectThat
//import strikt.assertions.hasSize
//import strikt.assertions.isEqualTo
//
//class InAppMessageEvaluationTest {
//
//    @Test
//    fun `create`() {
//
//        val context = Evaluators.context()
//        context.add(mockk<Evaluator.Evaluation>())
//        val message = mockk<InAppMessage.Message>()
//
//        val evaluation = InAppMessageEvaluation.of(
//            DecisionReason.DEFAULT_RULE,
//            context,
//            message
//        )
//
//        expectThat(evaluation) {
//            get { this.reason } isEqualTo DecisionReason.DEFAULT_RULE
//            get { this.targetEvaluations } hasSize 1
//            get { this.message } isEqualTo message
//        }
//    }
//}
