/*
 * 	Corbeans Yo! Cordapp: Sample/Template project for Corbeans,
 * 	see https://manosbatsis.github.io/corbeans
 *
 * 	Copyright (C) 2018 Manos Batsis.
 * 	Parts are Copyright 2016, R3 Limited.
 *
 * 	This library is free software; you can redistribute it and/or
 * 	modify it under the Apache License, Version 2.0 (the "License");
 * 	you may not use this file except in compliance 	with the License.
 * 	You may obtain a copy of the License at
 *
 * 	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * 	Unless required by applicable law or agreed to in writing,
 * 	software distributed under the License is distributed on an
 * 	"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * 	KIND, either express or implied.  See the License for the
 * 	specific language governing permissions and limitations
 * 	under the License.
 */
package mypackage.cordapp.workflow


import com.github.manosbatsis.partiture.flow.util.PartitureAccountsAwareFlow
import com.github.manosbatsis.vaultaire.annotation.VaultaireFlowResponder
import mypackage.cordapp.contract.YoContract
import net.corda.core.flows.InitiatingFlow
import net.corda.core.flows.StartableByRPC

/** Create a Yo! state to send a Yo! message */
@InitiatingFlow
@StartableByRPC
@VaultaireFlowResponder(YoFlowResponder::class)
class CreateYoFlow(
        input: YoStateClientDto
): PartitureAccountsAwareFlow<YoStateClientDto, YoStateClientDto>(
        // We use generated DTOs as input/output
        input = input,
        // Our custom input converter
        inputConverter = YoInputConverter(YoContract.Commands.Send()),
        // Our custom output converter
        outputConverter = YoOutputConverter())


/** Update a Yo! state to reply to the Yo! message */
@InitiatingFlow
@StartableByRPC
@VaultaireFlowResponder(YoFlowResponder::class)
class UpdateYoFlow(
        input: YoStateClientDto
): PartitureAccountsAwareFlow<YoStateClientDto, YoStateClientDto>(
        // We use generated DTOs as input/output
        input = input,
        // Our custom input converter
        inputConverter = YoInputConverter(YoContract.Commands.Reply()),
        // Our custom output converter
        outputConverter = YoOutputConverter())


