/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.dao.burningman.accounting.blockchain;

import bisq.core.dao.burningman.BurningManPresentationService;

import bisq.common.proto.network.NetworkPayload;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import static com.google.common.base.Preconditions.checkArgument;

// Outputs get pruned to required number of outputs depending on tx type.
// We store value as integer in protobuf as we do not support larger amounts than 21.47483647 BTC for our tx types.
// Name is burningman candidate name. For legacy Burningman we shorten to safe space.
@Slf4j
@EqualsAndHashCode
public final class AccountingTxOutput implements NetworkPayload {
    private static final String LEGACY_BM_FEES_SHORT = "LBMF";
    private static final String LEGACY_BM_DPT_SHORT = "LBMD";

    @Getter
    private final long value;
    private final String name;

    public AccountingTxOutput(long value, String name) {
        this.value = value;
        this.name = maybeShortenLBM(name);
    }

    private String maybeShortenLBM(String name) {
        return name.equals(BurningManPresentationService.LEGACY_BURNING_MAN_BTC_FEES_NAME) ?
                LEGACY_BM_FEES_SHORT :
                name.equals(BurningManPresentationService.LEGACY_BURNING_MAN_DPT_NAME) ?
                        LEGACY_BM_DPT_SHORT :
                        name;
    }

    public String getName() {
        return name.equals(LEGACY_BM_FEES_SHORT) ?
                BurningManPresentationService.LEGACY_BURNING_MAN_BTC_FEES_NAME :
                name.equals(LEGACY_BM_DPT_SHORT) ?
                        BurningManPresentationService.LEGACY_BURNING_MAN_DPT_NAME :
                        name;
    }


    ///////////////////////////////////////////////////////////////////////////////////////////
    // PROTO BUFFER
    ///////////////////////////////////////////////////////////////////////////////////////////

    @Override
    public protobuf.AccountingTxOutput toProtoMessage() {
        checkArgument(value < Integer.MAX_VALUE,
                "We only support integer values in protobuf storage for the amount.");
        return protobuf.AccountingTxOutput.newBuilder()
                .setValue((int) value)
                .setName(name).build();
    }

    public static AccountingTxOutput fromProto(protobuf.AccountingTxOutput proto) {
        int intValue = proto.getValue();
        checkArgument(intValue >= 0, "Value must not be negative");
        return new AccountingTxOutput(intValue, proto.getName());
    }

    @Override
    public String toString() {
        return "AccountingTxOutput{" +
                ",\r\n                    value=" + value +
                ",\r\n                    name='" + name + '\'' +
                "\r\n}";
    }
}
