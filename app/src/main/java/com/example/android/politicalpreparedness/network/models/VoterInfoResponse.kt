package com.example.android.politicalpreparedness.network.models

import com.example.android.politicalpreparedness.data.VoterData
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class VoterInfoResponse(
    val election: Election,
    val pollingLocations: String? = null, //TODO: Future Use
    val contests: String? = null, //TODO: Future Use
    val state: List<State>? = null,
    val electionElectionOfficials: List<ElectionOfficial>? = null,
)

fun VoterInfoResponse.asVoterInformation(): VoterData {
    val electionInfo = this.state?.first()?.electionAdministrationBody
    return VoterData(
        voterName = electionInfo?.name ?: "",
        electionInformationUrl = electionInfo?.electionInfoUrl ?: "",
        pollingStationFinderUrl = electionInfo?.votingLocationFinderUrl ?: "",
        ballotInformationUrl = electionInfo?.ballotInfoUrl ?: "",
        voterCorrespondenceAddress = electionInfo?.correspondenceAddress?.toFormattedString() ?: ""
    )
}