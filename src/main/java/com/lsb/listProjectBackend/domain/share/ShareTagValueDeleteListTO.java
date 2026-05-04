package com.lsb.listProjectBackend.domain.share;

import java.util.List;

public record ShareTagValueDeleteListTO(String shareTagId, List<String> values) {
}
