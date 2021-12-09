class TimeVector:
    def __init__(self):
        self._data = []

    def append(self, element):
        self._data.append(element)

    def tuple(self):
        return tuple(self._data)

    def __getitem__(self, item):
        return self._data[item]

    def __setitem__(self, key, value):
        self._data[key] = value

    def __len__(self):
        return len(self._data)

    def __lt__(self, other):
        if len(self) != len(other):
            return False

        has_lesser = False
        for a, b in zip(self, other):
            if a < b:
                has_lesser = True
            if a > b:
                return False

        return has_lesser

    def __gt__(self, other):
        if len(self) != len(other):
            return False

        has_greater = False
        for a, b in zip(self, other):
            if a > b:
                has_greater = True
            if a < b:
                return False

        return has_greater

    def __repr__(self):
        return tuple(self._data).__repr__()
