class Reading:
    def __init__(self, reading_id, co):
        self.id = reading_id
        self.co = float(co) if co != '' else 0

    def __repr__(self) -> str:
        return f'(id: {self.id}, CO: {self.co})'
