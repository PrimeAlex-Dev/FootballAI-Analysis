import { useList } from '../shared/useList'
import { seasonApi } from './api'
import SeasonForm from './SeasonForm'
import {
  Button,
  Field,
  ListTable,
  PageHeading,
  Pagination,
  RowActions,
} from '../shared/ui'
import { useRecordDialogs } from '../shared/useRecordDialogs'
export default function SeasonsPage() {
  const list = useList(seasonApi.list)
  const records = useRecordDialogs(seasonApi, list.refresh)
  return (
    <>
      <PageHeading
        title="Mùa giải"
        description="Danh mục mùa giải độc lập, dùng chung giữa các giải đấu."
        createLabel="Thêm mùa giải"
        onCreate={records.create}
      />
      {records.notice && (
        <p className="success-notice" role="status">
          {records.notice}
        </p>
      )}
      <section className="ui-card management-card">
        <form
          className="filter-bar"
          onSubmit={(event) => {
            event.preventDefault()
            list.change({
              name: String(
                new FormData(event.currentTarget).get('name') || '',
              ).trim(),
            })
          }}
        >
          <Field label="Tên mùa giải">
            <input
              key={list.params.get('name')}
              defaultValue={list.params.get('name') || ''}
              name="name"
              placeholder="Tìm theo tên, ví dụ 2026…"
            />
          </Field>
          <Button type="submit">Tìm kiếm</Button>
          <Button
            secondary
            onClick={(event) => {
              event.currentTarget.form?.reset()
              list.change({ name: '' })
            }}
          >
            Đặt lại
          </Button>
        </form>
        <ListTable
          headers={['ID', 'Tên mùa giải', 'Thao tác']}
          loading={list.loading}
          error={list.error}
          empty={!list.data?.content.length}
          retry={list.refresh}
        >
          {list.data?.content.map((item) => (
            <tr key={item.id}>
              <td>#{item.id}</td>
              <td>
                <strong>{item.name}</strong>
              </td>
              <td>
                <RowActions
                  view={() => records.select('view', item)}
                  edit={() => records.select('edit', item)}
                  remove={() => records.select('delete', item)}
                />
              </td>
            </tr>
          ))}
        </ListTable>
        <Pagination
          page={list.page}
          size={list.size}
          total={list.data?.page.totalElements || 0}
          pages={list.data?.page.totalPages || 0}
          disabled={list.loading || !!list.error}
          changeSize={(size) => list.change({ size })}
          go={list.go}
        />
      </section>
      {(records.creating || records.selection?.mode === 'edit') && (
        <SeasonForm
          value={records.selection?.value}
          onClose={records.close}
          onSuccess={records.success}
        />
      )}
      {records.dialogs(
        (item) => item.name,
        (item) => (
          <>
            <dt>ID</dt>
            <dd>#{item.id}</dd>
            <dt>Tên mùa giải</dt>
            <dd>{item.name}</dd>
          </>
        ),
      )}
    </>
  )
}
