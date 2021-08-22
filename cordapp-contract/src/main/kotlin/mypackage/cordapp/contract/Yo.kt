package mypackage.cordapp.contract

import com.github.manosbatsis.corda.leanstate.annotation.LeanStateModel
import com.github.manosbatsis.vaultaire.dto.AccountParty
import javax.persistence.Column

/** Our contract/persistent state source */
@LeanStateModel(
        contractClass = YoContract::class,
        contractStateName = "YoState",
        tableName = "yos",
        persistentStateName = "PersistentYoState",
        migrationResource = "yo-state-schema-v1.changelog-master"
)
interface Yo {
    val origin: AccountParty
    val target: AccountParty
    val message: String
    @get:Column(name = "reply_message", length = 500)
    val replyMessage: String?
}