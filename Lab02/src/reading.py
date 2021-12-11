class Reading:
    def __init__(self, reading_id, co):
        self.id = reading_id
        self.co = float(co) if co != '' else 0

    def __repr__(self) -> str:
        return f'(id: {self.id}, CO: {self.co})'

    def __eq__(self, other):
        return self.id == other.id and self.co == other.co

    def __hash__(self) -> int:
        return hash((self.id, self.co))
